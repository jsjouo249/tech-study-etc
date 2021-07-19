import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

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

	private static MongoCollection<Document> collection;

	public static MongoCollection<Document> getMongoCollection() {
		init();
		return collection;
	}

	private static void init(){

		String IP = "localhost";
		int PORT = 27017;
		String dbNm = "sampleDB";
		String collectionNm = "siteInfo";

		mongoClient = null;
		mongodb 	= null;
		collection  = null;

		try {

			mongoClient = new MongoClient( new ServerAddress( IP, PORT ),
										   MongoClientOptions.builder()
										   .serverSelectionTimeout( 5000 )	//서버 연결  시간 5초
										   .build()
										 );

			//연결된 클라이언트 정보
			if( "".equals( mongoClient.getConnectPoint() ) ) {
				Exception e = new Exception( "Check DB Connect Info" );
				throw new Exception();
			}

			mongodb = mongoClient.getDatabase( dbNm );

			if( null == mongodb.listCollections().first() ) {
				Exception e = new Exception( "Check Collection Name" );
				throw e;
			}

			collection = mongodb.getCollection( collectionNm );

			if( collection.count() == 0 ) {
				Exception e = new Exception( "Check COLLECTION" );
				throw e;
			}

		} catch( Exception e ) {
			System.out.println( e.getMessage() );
			mongodb    = null;
			collection = null;
			mongoClient.close();
		}
	}

	public static void find() {

		MongoCursor<Document> cur = collection.find().iterator();

		int size = 0;

		try {
			while( cur.hasNext() ) {
				Document bb = cur.next();
				String a = bb.toJson();
				System.out.println( size + " : " + a );
				size++;
			}

		} finally {
			cur.close();
		}
	}

	public static void insert( HashSet<siteInfo> siteListSet ) {

		ArrayList<Document> docs = new ArrayList<>();

		Iterator<siteInfo> iter = siteListSet.iterator();

		while( iter.hasNext() ) {
			siteInfo siteInfo = iter.next();

			docs.add( new Document( "siteUrl"	   , siteInfo.siteUrl )
					   	   .append( "siteTitle"	   , siteInfo.siteTitle )
					   	   .append( "siteExplain"  , siteInfo.siteExplain )
					   	   .append( "searchContent", siteInfo.searchContent )
				);
		}

		collection.insertMany( docs );

	}

}
