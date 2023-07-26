import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class Server implements Runnable{
    public Server(){
        done=false;
    }
    ServerSocket server;
    Socket client;
    boolean done;
    ExecutorService pool;
    @Override
    public void run() {
        try{
            server=new ServerSocket(4040);
            while(!done){
                client=server.accept();
                pool= Executors.newCachedThreadPool();
                ClientHandler handler=new ClientHandler(client);
                pool.execute(handler);
            }
        }catch(Exception e){
            System.out.println("Port is busy");
        }
    }
    class ClientHandler extends Thread{
        Socket client;
        BufferedReader in;
        PrintStream out;
        public ClientHandler(Socket client){
            this.client=client;
        }

        public void run(){
            try{
                in=new BufferedReader(new InputStreamReader(client.getInputStream()));
                out=new PrintStream(client.getOutputStream(),true);
                Connection con= DriverManager.getConnection("jdbc:mysql://localhost:3306","root","Malik@123");
                Statement stm=con.createStatement();
                while(!done){
                    out.println("1.Create Account");
                    out.println("2.Login");
                    out.println("3.Exit");
                    int ch= Integer.parseInt(in.readLine());
                    System.out.println(ch);
                    switch (ch){
                        case 1->{
                            stm.execute("use Bank");
                            stm.execute("create table if not exists Customer(acnum INTEGER NOT NULL AUTO_INCREMENT," +
                                    "name TEXT,dob DATE,pan TEXT,password TEXT,balance DOUBLE,PRIMARY KEY (acnum))");
                            stm.execute("alter table Customer AUTO_INCREMENT=1000");
                            PreparedStatement pstm=con.prepareStatement("insert into Customer(name,dob,pan,password,balance) values(?,?,?,?,0)",Statement.RETURN_GENERATED_KEYS);
                            out.println("Enter Your Name:");
                            String name=in.readLine();
                            out.println("Enter Your Date of Birth(dd-mm-yyyy):");
                            String dt=in.readLine();
                            DateFormat df=new SimpleDateFormat("dd-MM-yyyy");
                            Date dobj=df.parse(dt);
                            long time=dobj.getTime();
                            java.sql.Date date=new java.sql.Date(time);
                            out.println("Enter PAN number:");
                            String pan=in.readLine();
                            out.println("Enter Password:");
                            String pass=in.readLine();
                            pstm.setDate(2,date);
                            pstm.setString(1,name);
                            pstm.setString(3,pan);
                            pstm.setString(4,pass);
                            int test=pstm.executeUpdate();
                            if(test>0){
                                try {
                                    ResultSet res = pstm.getGeneratedKeys();
                                    System.out.println("end...");
                                    if (res.next()) {
                                        int ac = res.getInt(1);
                                        out.println("Your Account Number is "+ac);
                                    }
                                }catch(Exception e){
                                    System.out.println(e.getMessage());
                                    System.out.println("Some Error occured.....");
                                }
                            }
                            out.println("Remember it ......");
                        }
                        case 2->{
                                out.println("Enter Account Number:");
                                int ac=Integer.parseInt(in.readLine());
                                out.println("Enter Your Password:");
                                String pass=in.readLine();
                                boolean t=true;
                            while(t){
                                try{
                                    stm.execute("use Bank");
                                    ResultSet rs=stm.executeQuery("select * from Customer where acnum="+ac);
                                    rs.next();
                                    if(ac==rs.getInt("acnum") && pass.equals(rs.getString("password")))
                                    {
                                        out.println("Name :"+rs.getString("name"));
                                        out.println("Date Of Birth :"+rs.getDate("dob"));
                                        out.println("Balance :"+rs.getDouble("balance"));
                                        out.println("Choose an Option:");
                                        out.println("1.Deposit");
                                        out.println("2.Withdraw");
                                        out.println("3.Logout");
                                        int op= Integer.parseInt(in.readLine());
                                        if(op==1){
                                            out.println("Enter ammount:");
                                            double amt=Double.parseDouble(in.readLine());
                                            stm.executeUpdate("update Customer set balance="+amt+" where acnum="+rs.getInt("acnum"));
                                        }
                                        else if(op==2){
                                            out.println("Enter ammount:");
                                            double amt=Double.parseDouble(in.readLine());
                                            if(rs.getDouble("balance")>amt && rs.getDouble("balance")>0){
                                                stm.executeUpdate("update Customer set balance="+(rs.getDouble("balance")-amt)+" where acnum="+rs.getInt("acnum"));
                                            }else{
                                                out.println("Insufficent balance.....");
                                            }
                                        }
                                        else{
                                            t=false;
                                            break;
                                        }

                                    }else{
                                        out.println("No Account Found");
                                        t=false;
                                    }
                                }catch (Exception e){
                                    out.println("You are Not Authorised....!");
                                }
                            }
                            break;
                        }
                        case 3->{
                            out.println("Do You Want to Exit?(yes/no)");
                            String msg=in.readLine();
                            if(msg.equals("yes")||msg.equals("y")){
                                out.println("quit");
                            }
                        }
                    }
                }
            }catch(Exception e){
                out.println("Sorry Can't connect ");
                System.out.println(e.getMessage());
            }
        }

    }

    public static void main(String[] args) {
        Server server=new Server();
        server.run();
    }
}
