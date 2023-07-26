import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class Client implements Runnable{
    private Socket client;
    private BufferedReader in;
    private PrintStream out;
    private boolean done;
    @Override
    public void run() {
        try{
            client=new Socket("localhost",4040);
            out=new PrintStream(client.getOutputStream(),true);
            in=new BufferedReader(new InputStreamReader(client.getInputStream()));
            InputHandler inputHandler=new InputHandler();
            Thread t=new Thread(inputHandler);
            t.start();
            String message;
            while((message=in.readLine())!=null){
                if(message.equals("quit")){
                    shutdown();
                }
                System.out.println(message);
            }
        }catch(Exception e){
            System.out.println(e);
            //TODO:handle
        }
    }

    public void shutdown(){
        done=true;
        try{
            in.close();
            out.close();
            if(!client.isClosed()){
                client.close();
            }
            System.exit(0);
        }catch(Exception ignored){}
    }
    class InputHandler extends Thread{
        @Override
        public void run() {
            try{
                BufferedReader keybr=new BufferedReader(new InputStreamReader(System.in));
                while(!done){
                    String message= keybr.readLine();
                    if(message.equals("quit")){
                        keybr.close();
                        shutdown();
                    }else{
                        out.println(message);
                    }
                }
            }catch(Exception e){
                System.out.println(e);
            }
        }
    }

    public static void main(String[] args) {
        Client client=new Client();
        client.run();
    }
}
