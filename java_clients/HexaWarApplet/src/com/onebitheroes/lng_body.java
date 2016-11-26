package com.onebitheroes;

public class lng_body {
    public String MAP="+: Map";
    public String HELP="Help";
    public String CLOSE_HELP="Close";
    public String STR_LOGIN="Login";
    public String CLICK_HERE="Click here";
    public String STR_REGISTER="Register";
    public String CLICK_HERE_MORE="to register.";
    public String STR_WELCOME="Welcome! Enter your login name and password to continue!";
    public String STR_NAME="Login name:";
    public String STR_PASS="Password:";
    public String STR_PASS2="Retype password:";
    public String STR_MAIL="E-Mail:";
    public String CLIENT_LANG = "EN";
    public String UNDO_LABEL = "Undo";
    public String MISSING_RESPONSES_STRING_1 = "Waiting for ";
    public String MISSING_RESPONSES_STRING_2 = " Player(s)";
    public String CREATE_ACCOUNT="Create account";
    public String UN_NULL="Error: 'Login name' field is empty.";
    public String PASS_NULL="Error: 'Password' field is empty.";
    public String MAIL_NULL="Error: 'E-Mail' field is empty.";
    public String PASSWORDS_DONT_MATCH="Error: Passwords don't match! Please try again.";
    
    public String endofgame[]={"Game Over", "The winner is", "You placed ", // 0 1 2
    "1st!", "2nd!", "3rd!", "4th!", "5th!", "6th!", "7th!", "8th!"}; // 3 4 5 6 7 8 9 10
    public String menu_txt_1[] ={"Account settings", "Login", "Rules", "How to play", "Exit"};
    public String other_txt[] = {"Connecting", "Loading types", // 01
        "Back",	"Connecting", // 23
        "Menu", "Sending your move", // 45
        "Querying opponents move", "Next", // 67
        "The selected account", "will be removed.", //89
        "Are you sure?", "Registering" }; // 10 11
    public String startTxt[] = {"The game", "has been started!",
        "Number of turns: ", "Turn nr. "};
    public String eofTurnInfo[] = { "Next", "Continue", "Skip"};
    public String attack[]= { "You have attacked", "the enemy!",
        "The enemy has attacked", "your army",
        "You have lost ", "You have won ",
        "the battle!",};
    public String divide[] = {"Divide army", " of ", "Divide", "Cancel",
        "5: Move", "0: Divide"};
    public String account[] = {"New Account", "Name: ", "Password: ", "E-Mail: ",
        "Selected account: "};
    public String accountMod[] = {"Select", "Modify", "Delete", "Register"};
    public String accountChanged[] = {"Your account", "has been stored.", "Do you want",
        "to register it?"};
    public String regInfo[] = { "Registration failed.",
        "Registration completed! Do you want to log in with this account?",
        "Registration failed. Password is invalid.",
        "Registration failed. Username is invalid username.",
        "Registration failed. Username is already in use.",
    };
    public String yesno[] = {"Yes", "No"};
    public String pause_txt[] = { "Continue", "Skip Turn", "Exit" };
    public String error_txt[][] = { {"Login failed!", "Bad username", "or password"}, // 0
        {"Unknown error"}, // 1
        {"Reading gametypes","failed!"}, // 2
        {"Invalid game!", ""}, // 3
        {"Login successful,", "waiting for opponents."}, // 4
        {"Invalid gametype!", ""}, // 5
        {"Invalid round.", "(Connection", "timeout?)"}, // 6
        {"", "Invalid move", "(This is a bug)"}, // 7
        {"Unable to connect", "to the network."}, // 8
        {"Server is full.", "Please try again", "later..."}, // 9
    };
    
    public String howtoplayTxt = "How to play\n\n" +
            "Movement: You can move on the map with the "+
            "cursor buttons or the 1 2 3 4 6 7 8 9 buttons. "+
            "You can select an army with the 5 button. After "+
            "selecting the army, you can move it with the "+
            "cursor buttons or the 1 2 3 4 6 7 8 9 buttons "+
            "to the destination, ending the movement "+
            "with the 5 button.\n\nYou get the result of "+
            "your movement at the end of the turn. You may "+
            "move part of your army. You can divide your army "+
            "with the 0 button. You can divide a moving army "+
            "too, in this case the original army continues "+
            "its movement, the detached army goes towards the new "+
            "destination.\n"+
            
            "You can also navigate on the game field by clicking with"+
            "the left mouse button. You can select an army by"+
            "clicking with the right mouse button. You can set the"+
            "path of movement with the left mouse button and finish"+
            "the movement by clicking the right mouse button again.\nYou can skip the various "+
            "info panels using the Enter key on the numerical keyboard.";
    
    public String rulesTxt = "Rules\n\nHexawars is a turn-based "+
            "multiplayer strategy game.\n\nThe goal of the game is to "+
            "occupy the forts with your armies. At the beginning every "+
            "player has the same number of forts, with an army in each. "+
            "There are empty forts on the rest of the game area.\n\n"+
            "Every turn lasts a limited time. The players make their "+
            "move simultaneously. You can determine the movement of your armies "+
            "for more turns in advance.\n\nYou can occupy a fort with your"+
            "army if you stop on it or pass trough it. If the fort was empty, "+
            "it will be yours without a battle.\n\nIf an army meets an enemy "+
            "army, the battle takes place automatically.\n\nThe players get "+
            "reinforcements every turn automatically in the occupied forts.\n\n"+
            "The game ends if one of the players occupies all of the forts, or the last "+
            "turn is over. In this case, the player with the most occupied forts wins, if "+
            "the number of occupied forts is equal, the player with more soldiers wins.";
    public String CLS[]={"joined", "", "vanquished", "Connection lost with"};
}
