import java.rmi.*;
import java.util.*;
import java.rmi.server.*;

public class TicTacToeImpl extends UnicastRemoteObject implements TicTacToeContract{
    private static final long serialVersionUID = 7526472295622776147L;
    
    //Data Structures   
    //(playerID : gameID + player1/player2)
    public static HashMap<Integer,List<Integer> > playerToGame = new HashMap<>();

    //(gameID : board_config + current_turn_playerID)
    public static HashMap<Integer,List<String> > gameToBoard = new HashMap<>();

    //(gameID : player1_response + player2_response)
    public static HashMap<Integer,List<Integer> > gameToResponse = new HashMap<>();

    //(gameID : player1ID + player2ID)
    public static HashMap<Integer,List<Integer> > gameToPlayers = new HashMap<>();

    //counters to generate new player and game IDs
    public static int playerIDcnt = 0, gameIDcnt = 100, totalPlayers = 0;
    
    TicTacToeImpl()throws RemoteException{
        super();
    }
    
    //register new player and assign board
    public List<Integer> registerPlayer(){
        playerIDcnt++;
        totalPlayers++;
        
        //return playerID and gameID
        List<Integer> res = new ArrayList<>();
        res.add(playerIDcnt);
        res.add(gameIDcnt);
        
        //store players corresponding to the game
        List<Integer> players = gameToPlayers.get(gameIDcnt);
        if(players == null) {
            players = new ArrayList<>();
            players.add(playerIDcnt);
            gameToPlayers.put(gameIDcnt, players);
        }
        else
            players.add(playerIDcnt);
        
        //store gameID and (1/2 ==> X/O) to playerToGame 
        //store new board and first_turn_playerID to gameToBoard
        List<Integer> playerInfo = new ArrayList<>();
        playerInfo.add(gameIDcnt);
        
        if(totalPlayers % 2 == 1) {
            List<String> temp = new ArrayList<>();
            temp.add("-------------------------------------------------");
            temp.add(Integer.toString(playerIDcnt));
            gameToBoard.put(gameIDcnt,temp);
            
            playerInfo.add(1);
            playerToGame.put(playerIDcnt,playerInfo);
        }
        else {
            playerInfo.add(2);
            playerToGame.put(playerIDcnt,playerInfo);
            gameIDcnt++;
        }
        return res;
    }

    //assign game to two online players
    public Integer assignGame(Integer playerID,Integer gameID){
        //return opponentID
        if(totalPlayers % 2 == 0) {
            List<Integer> players = gameToPlayers.get(gameID);
            Integer opponentID = (players.get(0).equals(playerID)) ? players.get(1) : players.get(0);
            return opponentID;
        }
        return -1;
    }

    //check if its the player's turn or the opponent's turn
    public Integer isItMyTurn(Integer gameID,Integer playerID,Integer opponentID) {
        if(gameToBoard.get(gameID).get(1).equals(Integer.toString(playerID)))
            return playerID;
        return -1;
    }

    //toggle the turn to the opponent
    public void toggleTurn(Integer gameID,Integer playerID,Integer opponentID){
        if(gameToBoard.get(gameID).get(1).equals(Integer.toString(playerID)))
            gameToBoard.get(gameID).set(1,Integer.toString(opponentID));
    }

    //register the move in the board once it is played
    public String registerMove(Integer gameID,Integer cell_number,Integer playerID) {
        String board = gameToBoard.get(gameID).get(0);
        Integer symbol = playerToGame.get(playerID).get(1);
        
        char[] tempStr = board.toCharArray(); 
   
        tempStr[cell_number - 1] = (symbol.equals(1)) ? 'X' : 'O';
        board = String.valueOf(tempStr);
        
        gameToBoard.get(gameID).set(0,board);
        return board;
    }

    //validate board after every move
    public String validateBoard(Integer gameID,Integer cell_number,Integer playerID) {
        
        if(cell_number.equals(-1)) {
            gameToBoard.get(gameID).set(0,"lose");
            return "lose";
        }
        
        char symbol = playerToGame.get(playerID).get(1).equals(1) ? 'X' : 'O';
        String board = gameToBoard.get(gameID).get(0);
        
        char[][] boardArray = new char[7][7];
        int k = 0,i,j;
        for(i = 0;i<7;++i) {
            for(j = 0;j<7;++j) {
                boardArray[i][j] = board.charAt(k++);
            }
        }
        
        int x = (cell_number - 1)/7, y = (cell_number - 1) % 7;

        //check corresponding row
        int auxRow = 1;
	    for (i = 0; i < 7; i++) {
            for(j = 0; j < 7; j++) {
                if(j == 6) continue;

                if (boardArray[i][j] == boardArray[i][j+1] && boardArray[i][j] == symbol) {
                    auxRow++;
                    if(auxRow == 4){
                        gameToBoard.get(gameID).set(0,"win");
                        return "win";
                    }
                }else{
                    auxRow = 1;
                }
            }
        }

        //check corresponding column
        int auxCol = 1;
        for (j = 0; j < 7; j++) {
            for (i = 0; i < 7; i++) {
                if(i == 6) continue;

                if (boardArray[i][j] == boardArray[i+1][j] && boardArray[i][j] == symbol) {
                    auxCol++;
                    if(auxCol == 4){
                        gameToBoard.get(gameID).set(0,"win");
                        return "win";
                    }
                }else{
                    auxCol = 1;
                }
            }
        }

        //check left diagonal
        if(boardArray[3][0] == symbol && boardArray[2][1] == symbol && boardArray[1][2] == symbol && boardArray[0][3] == symbol || 
            boardArray[4][0] == symbol && boardArray[3][1] == symbol && boardArray[2][2] == symbol && boardArray[1][3] == symbol || 
            boardArray[3][1] == symbol && boardArray[2][2] == symbol && boardArray[1][3] == symbol && boardArray[0][4] == symbol || 
            boardArray[5][0] == symbol && boardArray[4][1] == symbol && boardArray[3][2] == symbol && boardArray[2][3] == symbol ||
            boardArray[4][1] == symbol && boardArray[3][2] == symbol && boardArray[2][3] == symbol && boardArray[1][4] == symbol ||
            boardArray[3][2] == symbol && boardArray[2][3] == symbol && boardArray[1][4] == symbol && boardArray[0][5] == symbol ||
            boardArray[6][0] == symbol && boardArray[5][1] == symbol && boardArray[4][2] == symbol && boardArray[3][3] == symbol ||
            boardArray[5][1] == symbol && boardArray[4][2] == symbol && boardArray[3][3] == symbol && boardArray[2][4] == symbol ||
            boardArray[4][2] == symbol && boardArray[3][3] == symbol && boardArray[2][4] == symbol && boardArray[1][5] == symbol ||
            boardArray[3][3] == symbol && boardArray[2][4] == symbol && boardArray[1][5] == symbol && boardArray[0][6] == symbol ||
            boardArray[6][1] == symbol && boardArray[5][2] == symbol && boardArray[4][3] == symbol && boardArray[3][4] == symbol ||
            boardArray[5][2] == symbol && boardArray[4][3] == symbol && boardArray[3][4] == symbol && boardArray[2][5] == symbol ||
            boardArray[4][3] == symbol && boardArray[3][4] == symbol && boardArray[2][5] == symbol && boardArray[1][6] == symbol ||
            boardArray[6][2] == symbol && boardArray[5][3] == symbol && boardArray[4][4] == symbol && boardArray[3][5] == symbol ||
            boardArray[5][3] == symbol && boardArray[4][4] == symbol && boardArray[3][5] == symbol && boardArray[2][6] == symbol ||
            boardArray[6][3] == symbol && boardArray[5][4] == symbol && boardArray[4][5] == symbol && boardArray[3][6] == symbol) {
            gameToBoard.get(gameID).set(0,"win");
            return "win";
        }    
     

        //check right diagonal
        if(boardArray[3][0] == symbol && boardArray[4][1] == symbol && boardArray[5][2] == symbol && boardArray[6][3] == symbol || 
            boardArray[2][0] == symbol && boardArray[3][1] == symbol && boardArray[4][2] == symbol && boardArray[5][3] == symbol || 
            boardArray[3][1] == symbol && boardArray[4][2] == symbol && boardArray[5][3] == symbol && boardArray[6][4] == symbol || 
            boardArray[1][0] == symbol && boardArray[2][1] == symbol && boardArray[3][2] == symbol && boardArray[4][3] == symbol ||
            boardArray[2][1] == symbol && boardArray[3][2] == symbol && boardArray[4][3] == symbol && boardArray[5][4] == symbol ||
            boardArray[3][2] == symbol && boardArray[4][3] == symbol && boardArray[5][4] == symbol && boardArray[6][5] == symbol ||
            boardArray[0][0] == symbol && boardArray[1][1] == symbol && boardArray[2][2] == symbol && boardArray[3][3] == symbol ||
            boardArray[1][1] == symbol && boardArray[2][2] == symbol && boardArray[3][3] == symbol && boardArray[4][4] == symbol ||
            boardArray[2][2] == symbol && boardArray[3][3] == symbol && boardArray[4][4] == symbol && boardArray[5][5] == symbol ||
            boardArray[3][3] == symbol && boardArray[4][4] == symbol && boardArray[5][5] == symbol && boardArray[6][6] == symbol ||
            boardArray[0][1] == symbol && boardArray[1][2] == symbol && boardArray[2][3] == symbol && boardArray[3][4] == symbol ||
            boardArray[1][2] == symbol && boardArray[2][3] == symbol && boardArray[3][4] == symbol && boardArray[4][5] == symbol ||
            boardArray[2][3] == symbol && boardArray[3][4] == symbol && boardArray[4][5] == symbol && boardArray[5][6] == symbol ||
            boardArray[0][2] == symbol && boardArray[1][3] == symbol && boardArray[2][4] == symbol && boardArray[3][5] == symbol ||
            boardArray[1][3] == symbol && boardArray[2][4] == symbol && boardArray[3][5] == symbol && boardArray[4][6] == symbol ||
            boardArray[0][3] == symbol && boardArray[1][4] == symbol && boardArray[2][5] == symbol && boardArray[3][6] == symbol){
            gameToBoard.get(gameID).set(0,"win");
            return "win";
        }
          
        int emptyAmount = 0;
        for (i = 0; i < 7; i++) {
            for(j = 0; j < 7; j++) {
                if(boardArray[i][j] != 'O' && boardArray[i][j] != 'X') emptyAmount++;
            }
        }

        if(emptyAmount == 0) {
            gameToBoard.get(gameID).set(0,"draw");
            return "draw";
        }
        return "NoResult";      
    } 
    //store players' response after every finished game
    public void playersResponse(Integer gameID,Integer ans){        
        List<Integer> res = gameToResponse.get(gameID);
        if(res == null) {
            res = new ArrayList<>();
            res.add(ans);
            gameToResponse.put(gameID,res);
        }
        else
            res.add(ans);
    }

    //start a new game or end the current game based on the responses submitted by the players
    public Integer continueGame(Integer gameID,Integer playerID) {
        if(gameToResponse.get(gameID).size() != 2)
            return 0;
        if(gameToResponse.get(gameID).get(0).equals(1) && gameToResponse.get(gameID).get(1).equals(1)){
            String board = gameToBoard.get(gameID).get(0);
            if(board.equals("12345678910111213141516171819202122232425262728293031323334353637383940414243444546474849"))
                return 1;
            board = "12345678910111213141516171819202122232425262728293031323334353637383940414243444546474849";
            gameToBoard.get(gameID).set(0,board);
            gameToBoard.get(gameID).set(1,Integer.toString(playerID));
            return 1;
        }
        totalPlayers -= 2;
        return 2;
    }

    //check if the opponent has made a move
    public boolean madeMove(Integer gameID,Integer playerID) {
        return gameToBoard.get(gameID).get(1).equals(Integer.toString(playerID));
    }
    
    //returns the latest configuaration of the board 
    public String retrieveBoard(Integer gameID) {
        return gameToBoard.get(gameID).get(0);
    }
}