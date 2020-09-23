package tagging;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

/**
 * MongoDB based storage for Users and Tags.
 */
public class UserTagDatabase {

	final static String SERVER = "localhost";
	final static int PORT = 27017;
	final static String DB_NAME = "testdb";

	final static String DB_USERS_COLLECTION = "users";
	final static String DB_TAGS_COLLECTION = "tags";

	final static String DB_USERS_NAME_FIELD = "name";
	final static String DB_USERS_TAGS_FIELD = "tags";
	final static String DB_TAGS_NAME_FIELD = "name";
	final static String DB_ID_FIELD = "_id";

	private MongoClient mongoClient;
	private MongoCollection<Document> userCollection;
	private MongoCollection<Document> tagCollection;

	public UserTagDatabase() {
		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.SEVERE);

		mongoClient = new MongoClient(SERVER, PORT);
		MongoDatabase database = mongoClient.getDatabase(DB_NAME);
		this.userCollection = database.getCollection(DB_USERS_COLLECTION);
		this.tagCollection = database.getCollection(DB_TAGS_COLLECTION);
	}

	public User insertUser(String name, Set<Tag> tags) {
		List<String> tagNamesToAdd = tags.stream().map(tag -> tag.getName()).collect(Collectors.toList());
		Document document = new Document(DB_USERS_NAME_FIELD, name).append(DB_USERS_TAGS_FIELD, tagNamesToAdd);
		userCollection.insertOne(document);
		return new User(document.get(DB_ID_FIELD).toString(), document.getString(DB_USERS_NAME_FIELD), new HashSet<>());
	}

	public boolean deleteUser(String userID) {
		DeleteResult result = userCollection.deleteOne(new Document(DB_ID_FIELD, new ObjectId(userID)));
		return result.getDeletedCount() == 1;
	}

	public User getUser(String userID) {
		Document document = this.userCollection.find(eq(DB_ID_FIELD, new ObjectId(userID))).first();
		if (document == null) {
			return null;
		} else {
			return new User(document.get(DB_ID_FIELD).toString(), document.getString(DB_USERS_NAME_FIELD),
					getTagSetFromDocument(document));
		}
	}

	public List<User> getAllUsers() {
		List<User> allUsers = new ArrayList<User>();
		FindIterable<Document> documentIterator = this.userCollection.find();
		Iterator<Document> it = documentIterator.iterator();
		while (it.hasNext()) {
			String userID = it.next().get(DB_ID_FIELD).toString();
			allUsers.add(User.fromID(userID));
		}
		return allUsers;
	}

	public String getAllUsersAsString() {
		String result = "";
		FindIterable<Document> documentIterator = this.userCollection.find();
		Iterator<Document> it = documentIterator.iterator();
		while (it.hasNext()) {
			Document doc = it.next();
			result += String.format("ID: %-26s | Name: %-12s | Tags: %s\n", doc.get(DB_ID_FIELD).toString(),
					doc.get(DB_USERS_NAME_FIELD), getTagSetFromDocument(doc));
		}
		return result;
	}

	public String getAllTagsAsString() {
		String result = "";
		FindIterable<Document> documentIterator = this.tagCollection.find();
		Iterator<Document> it = documentIterator.iterator();
		while (it.hasNext()) {
			Document doc = it.next();
			result += String.format("ID: %-26s | Name: %-5s\n", doc.get(DB_ID_FIELD).toString(),
					doc.get(DB_TAGS_NAME_FIELD));
		}
		return result;
	}

	public boolean userHasTag(String userID, Tag tag) {
		FindIterable<Document> result = this.userCollection.find(eq(DB_ID_FIELD, new ObjectId(userID)))
				.filter(in(DB_USERS_TAGS_FIELD, tag.getName()));
		return result.first() != null;
	}

	public Set<Tag> getTagsForUser(String userID) {
		FindIterable<Document> result = this.userCollection.find(eq(DB_ID_FIELD, new ObjectId(userID)));
		return getTagSetFromDocument(result.first());
	}

	public boolean addTagsToUser(String userID, Set<Tag> tagsToAdd) {
		List<String> tagNamesToAdd = tagsToAdd.stream().map(tag -> tag.getName()).collect(Collectors.toList());
		Document doc = this.userCollection.findOneAndUpdate(eq(DB_ID_FIELD, new ObjectId(userID)),
				addEachToSet(DB_USERS_TAGS_FIELD, tagNamesToAdd));
		return doc != null && !getTagSetFromDocument(doc).containsAll(tagsToAdd);
	}

	public boolean removeTagsFromUser(String userID, Set<Tag> tagsToRemove) {
		List<String> tagNamesToRemove = tagsToRemove.stream().map(tag -> tag.getName()).collect(Collectors.toList());
		Document doc = this.userCollection.findOneAndUpdate(eq(DB_ID_FIELD, new ObjectId(userID)),
				pullAll(DB_USERS_TAGS_FIELD, tagNamesToRemove));
		return doc != null && !Collections.disjoint(getTagSetFromDocument(doc), tagsToRemove);
	}

	public Tag insertTag(String tagName) {
		Document document = new Document(DB_TAGS_NAME_FIELD, tagName);
		tagCollection.insertOne(document);
		return new Tag(document.get(DB_ID_FIELD).toString(), document.getString(DB_TAGS_NAME_FIELD));
	}

	public boolean deleteTag(String tagID) {
		DeleteResult result = tagCollection.deleteOne(new Document(DB_ID_FIELD, new ObjectId(tagID)));
		return result.getDeletedCount() == 1;
	}

	public Tag getTag(String tagName) {
		Document document = this.tagCollection.find(eq(DB_TAGS_NAME_FIELD, tagName)).first();
		if (document == null) {
			return null;
		} else {
			return new Tag(document.get(DB_ID_FIELD).toString(), document.getString(DB_TAGS_NAME_FIELD));
		}
	}

	public void close() {
		mongoClient.close();
	}

	private Set<Tag> getTagSetFromDocument(Document document) {
		HashSet<Tag> tags = new HashSet<>();
		ArrayList<?> tagsList = (ArrayList<?>) document.get(DB_USERS_TAGS_FIELD);
		for (Object tagName : tagsList) {
			tags.add(Tag.fromName((String) tagName));
		}
		return tags;
	}
}
