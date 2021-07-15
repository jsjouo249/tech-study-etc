import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class searchOrInsertWithDB {

	private static MongoClient mongoClient;
	private static MongoDatabase mongodb;

	public static MongoDatabase getMongoDb() {
		init();
		return mongodb;
	}

	private static void init(){

		String IP = "localhost";
		int PORT = 27017;
		String dbNm = "sampleDB";

		mongoClient = null;
		mongodb 	= null;

		try {

			mongoClient = new MongoClient( new ServerAddress( IP, PORT ),
										   MongoClientOptions.builder()
										   .serverSelectionTimeout( 5000 )	//서버 연결  시간 5000
										   .build()
										 );

			if( "".equals( mongoClient.getConnectPoint() ) ) {
				throw new Exception();
			}

			mongodb = mongoClient.getDatabase( dbNm );

		} catch( Exception e ) {
			System.out.println( e.getMessage() );
			mongodb = null;
			mongoClient.close();
		}
	}

	public static void find() {

		String collectionNm = "sampleSiteInfo";

		MongoCollection<Document> collection = mongodb.getCollection( collectionNm );

		MongoCursor<Document> cur = collection.find().iterator();

		int size = 0;

		try {
			while( cur.hasNext() ) {
				Document bb = cur.next();
				String a = bb.toJson();
				System.out.println( a );
				size++;
			}

			System.out.println( "SIZE : " + size );

		} finally {
			cur.close();
		}
	}

}
