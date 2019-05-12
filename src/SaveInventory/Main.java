package SaveInventory;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;

//                       _oo0oo_
//                      o8888888o
//                      88" . "88
//                      (| -_- |)
//                      0\  =  /0
//                    ___/`---'\___
//                  .' \\|     |// '.
//                 / \\|||  :  |||// \
//                / _||||| -:- |||||- \
//               |   | \\\  -  /// |   |
//               | \_|  ''\---/''  |_/ |
//               \  .-\__  '-'  ___/-. /
//             ___'. .'  /--.--\  `. .'___
//          ."" '<  `.___\_<|>_/___.' >' "".
//         | | :  `- \`.;`\ _ /`;.`/ - ` : | |
//         \  \ `_.   \_ __\ /__ _/   .-` /  /
//     =====`-.____`.___ \_____/___.-`___.-'=====
//                       `=---='
//     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//               佛祖保佑         永无BUG
public class Main extends PluginBase implements Listener{

    @Override
    public void onEnable() {
        File file = new File(this.getDataFolder()+"/Players");
        if(!file.exists()){
            if(!file.mkdirs())
                Server.getInstance().getLogger().info("文件夹创建失败");
        }
        this.getServer().getPluginManager().registerEvents(this,this);
    }

    private File getPlayerFile(String name){
        return new File(this.getDataFolder()+"/Players/"+name+".json");
    }

    private Config getPlayerConfig(String name){
        return new Config(this.getPlayerFile(name),Config.JSON);
    }


    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent event){
        LinkedHashMap<String,Object> Inventory = new LinkedHashMap<>();
        Player player = event.getPlayer();
        for(int i = 0;i < player.getInventory().getSize()+4;i++){
            LinkedList<String> list = new LinkedList<>();
            Item item = player.getInventory().getItem(i);
            list.add(item.getId()+":"+item.getDamage());
            list.add(item.getCount()+"");
            String tag = item.hasCompoundTag() ?bytesToHexString(item.getCompoundTag()):"not";
            list.add(tag);
            Inventory.put(i+"",list);
        }
        Config config = getPlayerConfig(player.getName());
        config.setAll(Inventory);
        config.save();
    }

    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) throws IOException {
        Player player = event.getPlayer();
        if(!getPlayerFile(player.getName()).exists()) return;
        if(getPlayerConfig(player.getName()).getAll().isEmpty()) return;
        LinkedHashMap<String,Object> playerInventory = (LinkedHashMap<String, Object>) getPlayerConfig(player.getName()).getAll();
        for(int i = 0;i< player.getInventory().getSize() + 4;i++){
            ArrayList list = (ArrayList) playerInventory.get(i+"");
            String[] id = ((String) list.get(0)).split(":");
            Item item = new Item(Integer.parseInt(id[0]),
                    Integer.parseInt(id[1]),
                    Integer.parseInt(String.valueOf(list.get(1))));
            if(!String.valueOf(list.get(2)).equals("not")){
                CompoundTag tag = Item.parseCompoundTag(hexStringToBytes((String)list.get(2)));
                item.setNamedTag(tag);
            }
            player.getInventory().setItem(i,item);
        }
        File file = getPlayerFile(player.getName());
        if(file.exists())
            if(!file.delete()) {
                Server.getInstance().getLogger().info("清除失败");
            }
    }

    private static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }



}
