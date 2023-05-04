import java.rmi.*;
import java.util.*;
import java.io.*;


public class MyClient{
    
    //print board in a grid form
    public static void printBoard(String board){
        char[] boardArray = board.toCharArray();
        int count = 0;
        for (int i = 0; i < 49; i++) {
            String a = (i + 1) + "";
            if(i < 9) a = ("0" + (i + 1)) + "";
            if(boardArray[i] == 'X' || boardArray[i] == 'O') a =  " " + boardArray[i];

            System.out.print(a + " | ");
            if(count >= 6) {
                System.out.print("\n________________________________\n");
                count = -1;
            }
            count++;
        }
        System.out.print('\n');
    }
    public static void main(String args[]){
        try{

            Scanner in = new Scanner(System.in);

            String rmiAddress;
            System.out.println("Insert the server IP? (localhost) ");
            String address = in.nextLine();
            if (!address.isEmpty()) {
                rmiAddress = "rmi://" + address + ":1099/deadpool";
            } else {
                rmiAddress = "rmi://localhost:1099/deadpool";
            }

            TicTacToeContract stub = (TicTacToeContract)Naming.lookup(rmiAddress);

            System.out.println("Do you want to start the game ? (y/n) ");
            String reply = in.nextLine();
            if(!reply.equals("y"))
                return;
            
            //Register new player and assign board
            List<Integer> playerInfo = stub.registerPlayer();
            Integer playerID = playerInfo.get(0);
            Integer gameID = playerInfo.get(1);
            Integer opponentID = -1;

            System.out.println("Player"+Integer.toString(playerID)+"\nWait...");
            //keep checking for pairing a player
            while(true){
                Integer res = stub.assignGame(playerID,gameID);
                if(!res.equals(-1)){
                    opponentID = res;
                    break;
                }
            }
            System.out.println("Pairing successful...\nOpponent is Player"+Integer.toString(opponentID)+"...");

            while(true) {
                Integer turn_playerID = stub.isItMyTurn(gameID,playerID,opponentID);
                if(turn_playerID.equals(playerID)) {
                    String board = stub.retrieveBoard(gameID);
                    Integer cell_number = -1;
                    printBoard(board);
                    System.out.println("Enter cell number: ");
                    
                    //check for user imput before 10 seconds
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    while (!br.ready()){}
                    if (br.ready()) {
                        boolean isCellEmpty = false;
                        char[] boardArray = stub.retrieveBoard(gameID).toCharArray();
                        while(!isCellEmpty){
                            cell_number = Integer.parseInt(br.readLine());
                            if(cell_number-1 < 0 || cell_number-1 > 48 ){
                                System.out.println("Cell does not exist, try again...");
                            }else if(boardArray[cell_number-1] != '-') {
                                System.out.println("Cell is not empty, try again...");
                            }else{
                                isCellEmpty = true;
                            }
                        }
                        
                        String res = "";
                        board = stub.registerMove(gameID,cell_number,playerID);
                        printBoard(board);
                        res = stub.validateBoard(gameID,cell_number,playerID);  
                        stub.toggleTurn(gameID,playerID,opponentID);
                        //if the player's move results in a win
                        if(res.equals("win")){
                            System.out.println("WIN\nPlay another game ?");
                            //YES: 1    NO: 0
                            Integer ans = in.nextInt();
                            stub.playersResponse(gameID,ans);
                            while(true){
                                Integer t = stub.continueGame(gameID,playerID); 
                                if(t.equals(1)) {
                                    System.out.println("New game...");
                                    break;
                                }
                                else if(t.equals(2))
                                    System.exit(0);
                            }
                        }
                        //if the player's move results in a draw
                        else if(res.equals("draw")) {
                            System.out.println("DRAW\nPlay another game ?");
                            //YES: 1    NO: 0
                            Integer ans = in.nextInt();
                            stub.playersResponse(gameID,ans);
                            while(true){
                                Integer t = stub.continueGame(gameID,playerID); 
                                if(t.equals(1)) {
                                    System.out.println("New game...");
                                    break;
                                }
                                else if(t.equals(2))
                                    System.exit(0);
                            }
                        }
                    }
                    //Time out: user loses and prompts for new game
                    else {
                        System.out.println("TimeOut");
                        stub.validateBoard(gameID,-1,playerID);
                        stub.toggleTurn(gameID,playerID,opponentID);
                        System.out.println("LOSE\nPlay another game ?");
                        //YES: 1    NO: 0
                        Integer ans = in.nextInt();
                        stub.playersResponse(gameID,ans);
                        while(true){
                            Integer t = stub.continueGame(gameID,playerID); 
                            if(t.equals(1)) {
                                System.out.println("New game...");
                                break;
                            }
                            else if(t.equals(2))
                                System.exit(0);
                        }       
                        break;
                    }
                }
                else {
                    System.out.println("Wait for your turn...");
                    //while waiting check if oppoent's move brings any conclusive result
                    while(true) {
                        if(stub.madeMove(gameID,playerID)){
                            String res = stub.retrieveBoard(gameID);
                            if(res.equals("win")) {
                                System.out.println("LOSE\nPlay another game ?");
                                //YES: 1    NO: 0
                                Integer ans = in.nextInt();
                                stub.playersResponse(gameID,ans);
                                while(true){
                                    Integer t = stub.continueGame(gameID,playerID); 
                                    if(t.equals(1)) {
                                        System.out.println("New game...");
                                        break;
                                    }
                                    else if(t.equals(2))
                                        System.exit(0);
                                }                               
                            }
                            else if(res.equals("draw")){
                                System.out.println("DRAW\nPlay another game ?");
                                //YES: 1    NO: 0
                                Integer ans = in.nextInt();
                                stub.playersResponse(gameID,ans);
                                while(true){
                                    Integer t = stub.continueGame(gameID,playerID);
                                    if(t.equals(1)) {
                                        System.out.println("New game...");
                                        break;
                                    }
                                    else if(t.equals(2))
                                        System.exit(0);
                                }   
                            }
                            //Time out case: opponent loses and prompt for new game
                            else if(res.equals("lose")) {
                                System.out.println("WIN\nPlay another game ?");
                                //YES: 1    NO: 0
                                Integer ans = in.nextInt();
                                stub.playersResponse(gameID,ans);
                                while(true){
                                    Integer t = stub.continueGame(gameID,playerID);
                                    if(t.equals(1)) {
                                        System.out.println("New game...");
                                        break;
                                    }
                                    else if(t.equals(2))
                                        System.exit(0);
                                }   
                            }
                            break;
                        }
                    }
                }   
            }
        }catch(Exception e){System.out.println("ERROR");}
    }
    
}
