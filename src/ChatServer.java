import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class ChatServer {
    Socket socket;
    ServerSocket serverSocket;
    ChatServerThread chatServerThread;
    ArrayList<HashMap<String,ChatServerThread>> user_list;
    HashMap<String,ChatServerThread> user;
    int port;
    String userIdx;
    static String chatRoom,seller,buyer,sender,receiver,msg,time,postNumber,chatType;
    static JSONObject jsonObject;

    //JDBC연결
    static Connection con = null;
    static ResultSet rs = null;
    static PreparedStatement psmt = null;
    static Statement stmt = null;

    public ChatServer(int port){//ChatServer 생성자
        this.port = port;//ChatServer클래스 생성시에 포트번호를 입력받음
        try {
            serverSocket = new ServerSocket(port);//서버 생성
            user_list = new ArrayList();//사용자 담을 리스트 생성


            try {
                Class.forName("com.mysql.cj.jdbc.Driver");

                String url = "jdbc:mysql://3.37.128.131/novamarket";
                String id = "chunghyun";
                String pw ="ch656895!";

                con = DriverManager.getConnection(url,id,pw);

                stmt = con.createStatement();//statement 생성

                System.out.println("DB정상연결");


            } catch (ClassNotFoundException e) {
                System.out.println("DB연결실패");
                e.printStackTrace();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            while(true){//무한 반복으로 소켓 연결 및 스레드 실행
                socket = serverSocket.accept();//소켓 연결
                System.out.println("소켓 연결 완료");
                System.out.println("socket: " + socket);
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                userIdx = inputStream.readUTF();//소켓 연결 후 클라이언트에서 보낸 사용자 인덱스 저장
                System.out.println(userIdx+"번 사용자 접속");
                chatServerThread = new ChatServerThread(this);//클라이언트와 통신할 스레드 생성
                user = new HashMap<>();
                user.put(userIdx,chatServerThread);//사용자 번호와 해당 스레드를 해시맵에 담기
                user_list.add(user);//사용자 리스트에 사용자 데이터(해시맵) 담기
                chatServerThread.start();//스레드 시작
            }
        }catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public static void SaveChat(String chat){
        System.out.println("SaveChat: " +  chat);
    }
    public static void main(String[] args) {
        new ChatServer(8888);//클래스 생성
    }

    public static class SaveServer{
         public void SaveChat(String chat){
            System.out.println("SaveServer: " + chat);
            //클라이언트에서 수신받은 메세지를 JSON객체로 변환
            JSONParser parser = new JSONParser();
             Object obj = null;
             try {
                 obj = parser.parse(chat);
             } catch (ParseException e) {
                 e.printStackTrace();
             }
             jsonObject = (JSONObject) obj;

             chatRoom = (String) jsonObject.get("chatRoom");//방이름
             buyer = (String)jsonObject.get("buyer");//구매자
             seller = (String)jsonObject.get("seller");//판매자
             sender = (String)jsonObject.get("sender");//메세지 보낸 사람
             receiver = (String)jsonObject.get("receiver");//메세지 보낼 사람
             msg = (String)jsonObject.get("msg");//메세지
             postNumber = (String)jsonObject.get("postNumber");//게시글 번호
             chatType = (String)jsonObject.get("chatType");//채팅 타입 마켓인지 커뮤니티인지
             time = (String)jsonObject.get("time");//메시지 보낸 시간
             chatType = (String)jsonObject.get("type");//채팅 타입 msg/img/loc 중에 뭔지

             try {
                 rs = stmt.executeQuery("select * from chat_room where roomName = " + "'"+chatRoom+"'");
             } catch (SQLException throwables) {
                 throwables.printStackTrace();
             }

             // 채팅방 있는지 없는지 체크
             try {
                 Boolean a = rs.isBeforeFirst();

                 if(a){//채팅방 기존에 존재할때
                     System.out.println("있다");
                     int insertedCount2 = stmt.executeUpdate("INSERT INTO chat_msg (roomName,sender,msg,time,type) VALUES ('"+chatRoom+"','"+sender+"','"+msg+"','"+time+"','"+chatType+"')");
                     int insertedCount = stmt.executeUpdate("UPDATE chat_room SET rec_msg = '"+msg+"' ,rec_time = '"+time+"', type = '"+chatType+"' WHERE roomName = '"+chatRoom+"'");
                     System.out.println(chatRoom+"에 메세지 저장");
                 }else {//채팅방 없을때
                     System.out.println("없다");
                     //chat_room테이블에 채팅방 추가
                     int insertedCount = stmt.executeUpdate("INSERT INTO chat_room (roomName,buyer,seller,post_number,chat_type,rec_msg,rec_time,type) VALUES ('"+chatRoom+"','"+sender+"','"+seller+"','"+postNumber+"','market','"+msg+"','"+time+"','"+chatType+"')");
                     System.out.println("새로운 채팅방 생성");
                     int insertedCount2 = stmt.executeUpdate("INSERT INTO chat_msg (roomName,sender,msg,time,type) VALUES ('"+chatRoom+"','"+sender+"','"+msg+"','"+time+"','"+chatType+"')");
                     System.out.println(chatRoom+"에 메세지 저장");
                 }
             } catch (SQLException throwables) {
                 throwables.printStackTrace();
             }





         }
    }
}
