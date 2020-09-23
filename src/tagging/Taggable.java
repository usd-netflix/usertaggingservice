package tagging;

import java.util.Set;
import tagging.consistency.*;

/**
 * A taggable entity is one that stores and interacts with Tags.
 * It must implement the operations to get/add/remove Tags, as well as 
 * the consistency validation and fixer logic.
 */
public interface Taggable {

	public Set<Tag> getTags();
	
	/**
	 * Adds a set of tags to this entity.
	 * 
	 * @param tags the set of tags to add to this entity.
	 * @return true iff successful and made a change.
	 * @throws ConsistencyRuleViolationException if this operation would transition this entity from a consistent
	 * to an inconsistent state for any one of its consistency rules.
	 */
	public boolean addTags(Set<Tag> tags) throws ConsistencyRuleViolationException;
	
	/**
	 * 
	 * Removes a set of tags from this entity.
	 * 
	 * @param tags the set of tags to remove from this entity.
	 * @return true iff successful and made a change.
	 * @throws ConsistencyRuleViolationException if this operation would transition this entity from a consistent
	 * to an inconsistent state for any one of its consistency rules.
	 */
	public boolean removeTags(Set<Tag> tags) throws ConsistencyRuleViolationException;
	
	/**
	 * The set of consistency rules for this entity.
	 * An entity is considered consistent when all of its consistency rules evaluate to true.
	 */
	public Set<ConsistencyRule> getConsistencyRules();
	
	/**
	 * Validate for consistency.
	 * @return true iff this entity is consistent with all its rules.
	 */
	public default boolean validate() {
		for (ConsistencyRule rule : getConsistencyRules()) {
			if (!rule.evaluate(this)) {
				return false;
			}
		}

		return true;
	}
	
	/**
	 * 
	 * @throws ConsistencyFixerViolationException when this is run on an entity that is already consistent.
	 * @throws ConsistencyRuleViolationException when the fixer's changes do not make the entity consistent.
	 * 
	 * Note that the fixer logic is validated for consistency before the mutation is executed and committed.
	 */
	public default void fix() throws ConsistencyFixerViolationException, ConsistencyRuleViolationException {
		for (ConsistencyRule rule : getConsistencyRules()) {
			if (!rule.evaluate(this)) {
				rule.fix(this);
			}
		}
	}
}
