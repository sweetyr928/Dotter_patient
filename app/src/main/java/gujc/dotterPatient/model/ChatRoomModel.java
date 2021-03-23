package gujc.dotterPatient.model;

public class ChatRoomModel {
    private String roomID;
    private String title;
    private String photo;
    private String lastMsg;
    private String lastDatetime;
    private Integer userCount;
    private Integer unreadCount;
    private String board;
    private int identification; // 1: 등록 2: 수락 대기 중 3: 수락
    private String phone;

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    public int getIdentification() { return identification; }

    public void setIdentification(int identification) {
        this.identification = identification;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public String getLastDatetime() {
        return lastDatetime;
    }

    public void setLastDatetime(String lastDatetime) {
        this.lastDatetime = lastDatetime;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }


    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }
}
