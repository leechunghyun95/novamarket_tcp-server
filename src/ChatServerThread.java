import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatServerThread extends Thread{
    ServerSocket serverSocket;
    Socket socket;
    ArrayList<HashMap<String,ChatServerThread>> user_list;
    DataInputStream inputStream;
    DataOutputStream outputStream;
    String user;
    String chatRoom,seller,buyer,sender,receiver,msg,time,postNumber,chatType;
    ChatServer chatServer;
    JSONObject jsonObject;

    public ChatServerThread(ChatServer chatServer){//ChatServer를 생성자로 받아서 serversocket,socket,user_list 가져오기
        this.serverSocket = chatServer.serverSocket;
        this.socket = chatServer.socket;
        this.user_list = chatServer.user_list;
        this.user = chatServer.userIdx;
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    @Override
    public void run() {


        try{
            System.out.println("user_list: " + user_list);
            while(true){
                String chat = inputStream.readUTF();
                System.out.println(user+"번 사용자: " + chat);

                //클라이언트에서 수신받은 메세지를 JSON객체로 변환
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(chat);
                jsonObject = (JSONObject) obj;

                receiver = (String)jsonObject.get("receiver");//메세지 보낼 사람

                System.out.println(1);
                ChatServer.SaveServer saveServer = new ChatServer.SaveServer();
                System.out.println(2);
                saveServer.SaveChat(chat);
                System.out.println(3);
                for (int i = 0; i < user_list.size(); i++){
                    System.out.println(4);
                    ChatServerThread chatServerThread = user_list.get(i).get(receiver);

                    if(chatServerThread == null){
                        System.out.println(5);
                    }else {
                        System.out.println(6);
                        chatServerThread.outputStream.writeUTF(chat);
                        System.out.println(receiver+"번 사용자에게 전송");
                        System.out.println(chatServerThread.socket);
                    }
                }

            }
        } catch (EOFException e){
            System.out.println("EOF Exception occur");
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            for (int i = 0; i < user_list.size(); i++){
                ChatServerThread chatServerThread = user_list.get(i).get(user);
                if(chatServerThread == null){

                }else {
                    user_list.remove(i);
                    System.out.println("사용자 목록 삭제");
                    System.out.println("user_list: " + user_list);
                }
            }
            try {
                System.out.println("socket 종료: " + socket);
                socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }



    }
}
