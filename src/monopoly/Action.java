package monopoly;

public class Action {
    public enum ActionType {
        DICE, WALK, STAY
    }

    private static Action diceAction = new Action(ActionType.DICE, 0),
        stayAction = new Action(ActionType.STAY, 0);

    private ActionType type;
    private int steps;

    public Action(ActionType type, int steps) {
        setType(type);
        setSteps(steps);
    }

    public ActionType getType() {
        return type;
    }

    public int getSteps() {
        return steps;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public static Action getDiceAction() {
        return diceAction;
    }

    public static Action getStayAction() {
        return stayAction;
    }

    public static Action getWalkAction(int steps) {
        return new Action(ActionType.WALK, steps);
    }
}
