package me.MC_Elmo.utils;

import me.MC_Elmo.CommandStrings;
import org.bukkit.ChatColor;

/**
 * Created by Elom on 6/20/16.
 */
public class MainUtils
{
    private static final long DAY = 86400000L;
    private static final long HOUR = 3600000L;
    private static final long MINUTE = 60000L;
    private static final long SECOND = 1000L;
    private static final long TICK = 50L;
    private static final char DAY_CHAR = 'd', HOUR_CHAR = 'h', MINUTE_CHAR = 'm', SECOND_CHAR = 's', TICK_CHAR = 't';
    private CommandStrings plugin;
    public MainUtils(CommandStrings plugin)
    {
        this.plugin = plugin;
    }
    /**
     * This parses a String which holds a time and returns the duration in ms.
     * <p/>
     * <p><strong>Example:</strong> 2d5h2m52s</p>
     *
     * @param time The string to parse
     * @return The duration in seconds
     */
    public static long parseTime(String time)
    {
        char[] chars = time.toCharArray();

        long duration = 0;

        long current = 0;
        int run = 1;
        long multi = 1;

        //iterate backwards
        for (int i = chars.length - 1; i >= 0; i--) {
            char character = chars[i];
            int currentNumber = getDigit(character);

            if (currentNumber != -1) {
                //is number
                //set the value of the current iteration
                current += currentNumber * run;
                //add the current value to the whole
                duration += current * multi;
                //we set our run '* 10' because: 112 = 1 * 100 + 1 * 10 + 2 * 1
                run *= 10;
            } else {
                //is character
                //lets find our multiplicator
                switch (character) {
                    case DAY_CHAR:
                        multi = DAY;
                        break;
                    case HOUR_CHAR:
                        multi = HOUR;
                        break;
                    case MINUTE_CHAR:
                        multi = MINUTE;
                        break;
                    case SECOND_CHAR:
                        multi = SECOND;
                        break;
                    case TICK_CHAR:
                        multi = TICK;
                    case ' ':
                    case '_':
                        continue;
                    default:
                        throw new IllegalArgumentException(String.format("Invalid character found! %s", character));
                }
                //everytime we get a new character which signs the multiplicator we reset our values
                current = 0;
                run = 1;
            }
        }

        return duration;
    }

    /**
     * Returns the number of a character. '9' will return the integer 9, '2' will return the integer 2
     *
     * @param character The character to parse
     * @return The integer which matches, or -1 if the parse fails
     */
    public static int getDigit(int character)
    {
        final int MINIMAL_DIGIT = 48, MAXIMAL_DIGIT = 57;

        if (character >= MINIMAL_DIGIT && character <= MAXIMAL_DIGIT) {
            return character - MINIMAL_DIGIT;
        }

        return -1;
    }




    public void log(String message)
    {
        plugin.getLogger().info(colorize(plugin.getPrefix()) + message);
    }
    public void logSevere(String message){plugin.getLogger().severe(plugin.getPrefix() + message);}


    public String colorize(String input)
    {
        return ChatColor.translateAlternateColorCodes('&', input);
    }



}
