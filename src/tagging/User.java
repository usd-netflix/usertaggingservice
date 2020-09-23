package tagging;

import java.util.*;

import tagging.consistency.*;

public class User implements Taggable {
	
	private String id;
	private String name;
	private Set<Tag> tags;
	
	public User(String id, String name, Set<Tag> tags) {
		this.id = id;
		this.name = name;
		this.tags = tags;
	}
	
	public static User fromID(String userID) {
		UserTagDatabase udb = new UserTagDatabase();
		User user = udb.getUser(userID);
		udb.close();
		return user;
	}
	
	public static User create(String name, Set<Tag> tags) throws ConsistencyRuleViolationException {
		User temp = new User("", name, tags);
		for (ConsistencyRule rule : temp.getConsistencyRules()) {
			if (!rule.validateOperation(new HashSet<Tag>(), TaggableOperation.CREATE, tags)) {
				throw new ConsistencyRuleViolationException(rule);
			}
		}
		UserTagDatabase udb = new UserTagDatabase();
		User user = udb.insertUser(name, tags);
		udb.close();
		return user;
	}
	
	public String getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public Set<Tag> getTags() {
		UserTagDatabase udb = new UserTagDatabase();
		this.tags = udb.getTagsForUser(this.id);
		udb.close();
		return tags;
	}

	@Override
	public boolean addTags(Set<Tag> tags) throws ConsistencyRuleViolationException {
		for (ConsistencyRule rule : getConsistencyRules()) {
			if (!rule.validateOperation(this.getTags(), TaggableOperation.ADD_TAG, tags)) {
				throw new ConsistencyRuleViolationException(rule);
			}
		}

		UserTagDatabase udb = new UserTagDatabase();
		boolean result = udb.addTagsToUser(this.id, tags);
		udb.close();
		return result;
	}

	@Override
	public boolean removeTags(Set<Tag> tags) throws ConsistencyRuleViolationException {
		for (ConsistencyRule rule : getConsistencyRules()) {
			if (!rule.validateOperation(this.getTags(), TaggableOperation.REMOVE_TAG, tags)) {
				throw new ConsistencyRuleViolationException(rule);
			}
		}

		UserTagDatabase udb = new UserTagDatabase();
		boolean result = udb.removeTagsFromUser(this.id, tags);
		udb.close();
		return result;
	}

	@Override
	public Set<ConsistencyRule> getConsistencyRules() {
		return new HashSet<>(Arrays.asList(
			new NANDConsistencyRule(Tag.fromName("T1"), Tag.fromName("T4")),
			new NANDConsistencyRule(Tag.fromName("T2"), Tag.fromName("T3"))
		));
	}
}
