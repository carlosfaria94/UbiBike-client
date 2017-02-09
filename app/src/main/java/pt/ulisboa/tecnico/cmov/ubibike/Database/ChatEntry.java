package pt.ulisboa.tecnico.cmov.ubibike.Database;


import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatEntry
{
    private String id;
    private String friendName;
    private String chat;
    private String date;

    public ChatEntry(String friendName, String chat)
    {
        this.friendName = friendName;
        this.chat = chat;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        date = sdf.format(new Date());
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {

        return id;
    }

    public String getFriendName() {
        return friendName;
    }

    public String getChat() {
        return chat;
    }

    public String getDate() {
        return date;
    }

}
