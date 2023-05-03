import java.rmi.*;
//import java.rmi.registry.*;


public class MyServer{  
    public static void main(String args[]){
        try{
            TicTacToeContract stub = new TicTacToeImpl();
            Naming.rebind("rmi://localhost:1099/deadpool",stub);
        }catch(Exception e){System.out.println(e);}
    }
}