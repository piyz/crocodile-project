package by.matrosov.crocoproject.model.message;

public class ScoreMessage {
    private String[] usersScore;
    private String drawer;

    public String getDrawer() {
        return drawer;
    }

    public void setDrawer(String drawer) {
        this.drawer = drawer;
    }

    public String[] getUsersScore() {
        return usersScore;
    }

    public void setUsersScore(String[] usersScore) {
        this.usersScore = usersScore;
    }
}
