package io.github.sawors.slogger;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.sql.Time;
import java.time.LocalTime;
import java.util.logging.Level;

public class SLogger {
    
    private final int logColorLeft;
    private final int logColorRight;
    private final Plugin plugin;
    
    /**
     * Create a new instance with the default colors
     * @param plugin the minecraft plugin which will be used to prefix the messages
     */
    public SLogger(Plugin plugin){
        this(plugin,
                0xffee00,
                0xe600da
        );
    }
    
    public SLogger(Plugin plugin, int leftColor, int rightColor) {
        this.plugin = plugin;
        this.logColorLeft = leftColor;
        this.logColorRight = rightColor;
    }
    
    /**
     *
     * @param leftColor The new left color. Set to null to keep the old color.
     * @param rightColor The new right color. Set to null to keep the old color.
     * @return A new {@link SLogger} with the new colors.
     */
    @Contract("!null, !null -> new")
    public SLogger withColors(@Nullable Integer leftColor, @Nullable Integer rightColor){
        return new SLogger(this.plugin,leftColor != null ? leftColor : this.logColorLeft, rightColor != null ? rightColor : this.logColorRight);
    }
    
    /**
     * Warning : Please note that <b>this method is not designed for fast logging</b> as it has to fetch the stacktrace, which can impact performances. <b>To do quick successive logging with good performances please use logAdmin(object, true)</b> as it will disable all the slow features
     * @param object The object to print. This method will print the result of object.toString().
     */
    public void log(Object object){
        log(object,true);
    }
    
    /**
     *
     * @param object The object to print. This method will print the result of object.toString().
     * @param simplified Whether to use the simplified printing mode or not. <b>The simplified mode is much faster than the default one (up to 6 times faster)</b>
     */
    public void log(Object object, boolean simplified){
        log(object,simplified,!simplified,!simplified);
    }
    
    /**
     *
     * @param object The object to print. This method will print the result of object.toString().
     * @param sendPlayer Should online operators receive the message.
     * @param includeLine Should the console log contain the class and the line number where this method has been used. <i>(high performance impact)</i>
     * @param color Should the player output (if sendPlayer = true) be colored with a fancy gradient. <i>(low performance impact)</i>
     */
    public void log(Object object, boolean sendPlayer, boolean includeLine, boolean color) {
        
        String objectString = object != null ? getFormattedString(object) : "⚠ null ⚠";
        
        // TODO : proper integration of this
        String pluginname = plugin.getName();
        
        String anchor;
        
        if(includeLine){
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            StackTraceElement caller = stack[2];
            if(caller.getMethodName().equals("logAdmin")) caller = stack[3];
            anchor = "["+pluginname+" @"+caller.getClassName().substring(caller.getClassName().lastIndexOf(".")+1)+"."+caller.getMethodName()+"("+caller.getFileName()+":"+caller.getLineNumber()+")]";
        } else {
            anchor = "["+pluginname+"]";
        }
        
        Bukkit.getLogger().log(Level.INFO, anchor+" : "+objectString);
        
        if(sendPlayer){
            String playerAnchor = "["+pluginname+" - "+ Time.valueOf(LocalTime.now())+"]";
            Component coloredOutput = color ? gradientText(playerAnchor+" :",
                    logColorLeft,
                    logColorRight
            ).append(Component.text(" "+objectString).color(NamedTextColor.GRAY)) : Component.text(playerAnchor+" : "+objectString).color(NamedTextColor.YELLOW);
            for(Player p : Bukkit.getOnlinePlayers()){
                if(p.isOp()){
                    p.sendMessage(coloredOutput);
                }
            }
        }
    }
    
    private String getFormattedString(Object object){
        
        //TODO : Different formatting for specific classes
        
        return object.toString();
    }
    
    public static Component gradientText(String text, int fromColor, int toColor){
        String[] letters = text.split("");
        Color source = new Color(fromColor);
        Color target = new Color(toColor);
        Component output = Component.empty();
        int redStep = (target.getRed()-source.getRed())/letters.length;
        int greenStep = (target.getGreen()-source.getGreen())/letters.length;
        int blueStep = (target.getBlue()-source.getBlue())/letters.length;
        for(int i = 0; i<letters.length; i++){
            output = output.append(Component.text(letters[i]).color(TextColor.color(source.getRed()+(redStep*i),source.getGreen()+(greenStep*i),source.getBlue()+(blueStep*i))));
        }
        return output;
    }
    
    public static Component gradientText(String text, int... colors){
        if(colors.length == 1){
            return Component.text(text).color(TextColor.color(colors[0]));
        } else if(colors.length == 0){
            return Component.text(text).color(NamedTextColor.WHITE);
        }
        Component output = Component.empty();
        int step = (int) Math.ceil((double) text.length() /(colors.length-1));
        int i = 0;
        int colorIndex = 0;
        do{
            String sub = text.substring(i,Math.min(text.length(),i+step));
            output = output.append(gradientText(sub,colors[colorIndex],colors[colorIndex+1]));
            
            i+=step;
            colorIndex++;
        } while (i <= text.length() && colorIndex < colors.length-1);
        
        
        return output;
    }
}
