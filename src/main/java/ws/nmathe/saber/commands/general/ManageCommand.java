package ws.nmathe.saber.commands.general;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import ws.nmathe.saber.Main;
import ws.nmathe.saber.commands.Command;
import ws.nmathe.saber.commands.CommandInfo;
import ws.nmathe.saber.core.schedule.ScheduleEntry;
import ws.nmathe.saber.utils.MessageUtilities;
import ws.nmathe.saber.utils.ParsingUtilities;
import ws.nmathe.saber.utils.VerifyUtilities;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * used for generating the list of valid timezone strings
 */
public class ManageCommand implements Command
{
    @Override
    public String name()
    {
        return "manage";
    }

    @Override
    public CommandInfo info(String prefix)
    {
        String head = prefix + this.name();
        String usage = "``" + head + "`` - add or kick users from an event";
        CommandInfo info = new CommandInfo(usage, CommandInfo.CommandType.MISC);

        String cat1 = "- Usage\n" + head + " <id> <add|kick> <group> <@user>";
        String cont1 = "" +
                "This command may be used to manually add or remove particular users from an event's rsvp groups." +
                "\n\n" +
                "The ``<group>`` argument should be one of the RSVP groups configured for the schedule.\n" +
                "``<@user>`` should be an @mention for the user to be added/removed.\n" +
                "If ``<@user>`` is not an @mention for a user, a 'dummy' user will be used.";
        info.addUsageCategory(cat1, cont1);

        info.addUsageExample(head+" 10agj2 add Yes @Saber#9015");
        info.addUsageExample(head+" 10agj2 kick Yes @notem#1654");

        return info;
    }

    @Override
    public String verify(String prefix, String[] args, MessageReceivedEvent event)
    {
        String head = prefix + this.name();
        int index = 0;
        if (args.length < 3)
        {
            return "Incorrect amount of arguments!" +
                    "\nUse ``" + head + " <id> <add|kick> <group> <@user>``";
        }

        // verify valid entry ID
        ScheduleEntry entry;
        if (VerifyUtilities.verifyEntryID(args[index]))
        {
            Integer entryId = ParsingUtilities.encodeIDToInt(args[index]);
            entry = Main.getEntryManager().getEntryFromGuild(entryId, event.getGuild().getId());
            if (entry == null)
            {
                return "The requested entry does not exist!";
            }
        }
        else
        {
            return "Argument *" + args[0] + "* is not a valid entry ID!";
        }

        // verify the schedule has RSVP enabled
        if (!Main.getScheduleManager().isRSVPEnabled(entry.getChannelId()))
        {
            return "That event is not on an RSVP enabled schedule!";
        }

        // verify valid action argument
        index++;
        switch (args[index])
        {
            case "a":
            case "add":
            case "k":
            case "kick":
                break;

            default:
                return "*" + args[index] + "* is not a valid action argument!\n" +
                        "Please use either *add* or *kick*!";
        }

        // verify the group is a valid group
        index++;
        if (!entry.getRsvpMembers().keySet().contains(args[index]))
        {
            return "There is no RSVP group called *" + args[index] + "* on the event!";
        }

        return "";
    }

    @Override
    public void action(String prefix, String[] args, MessageReceivedEvent event)
    {
        int index = 0;
        Integer entryId = ParsingUtilities.encodeIDToInt(args[index++]);
        ScheduleEntry se = Main.getEntryManager().getEntryFromGuild(entryId, event.getGuild().getId());
        String logging = Main.getScheduleManager().getRSVPLogging(se.getChannelId());

        String content="", group, user;
        List<String> members;
        switch(args[index++])
        {
            case "a":
            case "add":
                group = args[index++];
                user = args[index].matches("<@!?\\d+>") ? args[index].replaceAll("[^\\d]", ""):args[index];
                members = se.getRsvpMembersOfType(group);
                members.add(user);
                se.setRsvpMembers(group, members);
                content = "I have added *" + args[index] + "* to *" + group + "* on event :id:" + ParsingUtilities.intToEncodedID(se.getId());

                // log the rsvp action
                if (!logging.isEmpty())
                {
                    String msg = "";
                    if(user.matches("[\\d]")) msg += "<@" + user + ">";
                    else msg += user;
                    msg += " has been added to the RSVP group *"+group+"* for **" +
                            se.getTitle() + "** - :id: **" + ParsingUtilities.intToEncodedID(se.getId()) + "**";
                    MessageUtilities.sendMsg(msg, event.getJDA().getTextChannelById(logging), null);
                }
                break;

            case "k":
            case "kick":
                group = args[index++];
                user = args[index].matches("<@!?\\d+>") ? args[index].replaceAll("[^\\d]", ""):args[index];
                members = se.getRsvpMembersOfType(group);
                members.remove(user);
                se.setRsvpMembers(group, members);
                content = "I have removed *" + args[index] + "* from *" + group + "* on event :id:" + ParsingUtilities.intToEncodedID(se.getId());

                // log the rsvp action
                if (!logging.isEmpty())
                {
                    String msg = "";
                    if(user.matches("[\\d]")) msg += "<@" + user + ">";
                    else msg += user;
                    msg += " has been kicked from the RSVP group *"+group+"* for **" +
                            se.getTitle() + "** - :id: **" + ParsingUtilities.intToEncodedID(se.getId()) + "**";
                    MessageUtilities.sendMsg(msg, event.getJDA().getTextChannelById(logging), null);
                }
                break;
        }

        Main.getEntryManager().updateEntry(se, false);
        MessageUtilities.sendMsg(content, event.getTextChannel(), null);
    }
}
