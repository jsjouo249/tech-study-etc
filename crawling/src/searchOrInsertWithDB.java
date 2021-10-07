import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.print.Doc;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;

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

	public static void find() throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter( System.out) );

		bw.write( "검색된 목록들\n" );

		MongoCursor<String> reCur = collection.distinct( "searchContent", String.class).iterator();

		while( reCur.hasNext() ) {
			bw.write( reCur.next() + "\n" );
		}

		bw.write("\n");

		bw.write( "검색어 입력\n" );
		bw.flush();
		String srchContent = br.readLine();

		//보여지는 컬럼
		Bson projectionFields = Projections.fields(
				Projections.include( "siteUrl", "siteTitle", "siteExplain", "searchContent" ),
				Projections.excludeId()
			);

		//FTS 인덱스 - siteTitle, siteExplain으로 n-gram 생성
		BasicDBObject query = new BasicDBObject();
		BasicDBObject subquery = new BasicDBObject();

		subquery.put( "$search" , srchContent );
		query.put(    "$text"   , subquery    );

		MongoCursor<Document> curFtsIndex  = collection.find( query ).projection( projectionFields ).iterator();

		//siteUrl like 검색
		BasicDBObject siteUrlQuery = new BasicDBObject();
		siteUrlQuery.put( "siteUrl" , Pattern.compile( srchContent ) );
		MongoCursor<Document> curSiteUrl   = collection.find( siteUrlQuery ).projection( projectionFields ).iterator();

		int size = 1;

		try {

			if( !curFtsIndex.hasNext() && !curSiteUrl.hasNext() ) {
				bw.write( "search Result : 0" );

			}else {

				HashSet<String> printHs = new HashSet<>();

				while( curFtsIndex.hasNext() ) {
					Document docFts = curFtsIndex.next();
					printHs.add( docFts.toJson() );
				}

				while( curSiteUrl.hasNext() ) {
					Document docUrl = curSiteUrl.next();
					printHs.add( docUrl.toJson() );
				}

				Iterator<String> iter = printHs.iterator();

				while( iter.hasNext() ) {
					bw.write( size + " : " + iter.next() + "\n" );
					size++;
				}
			}


		} finally {
			bw.flush();
			bw.close();
			curFtsIndex.close();
			curSiteUrl.close();
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
					   	   .append( "bigram"	   , siteInfo.bigram )
					   	   .append( "trigram"	   , siteInfo.trigram )
				);
		}

		collection.insertMany( docs );

		System.out.println( "Complete!" );

	}

}
