package tagging.consistency;

import java.util.HashSet;
import java.util.Set;

import tagging.Tag;
import tagging.TaggableOperation;
import tagging.Taggable;

public abstract class ConsistencyRule {

	Set<Tag> tags;
	
	// A consistency rule must operate on two or more tags.
	public ConsistencyRule(Tag tagA, Tag tagB, Tag... tags) {
		this.tags = new HashSet<Tag>();
		this.tags.add(tagA);
		this.tags.add(tagB);
		for (Tag tag : tags) {
			this.tags.add(tag);
		}
	}

	/**
	 * Evaluate the Taggable object for consistency.
	 * 
	 * @return true iff the Taggable object is consistent.
	 */
	public boolean evaluate(Taggable taggable) {
		Set<Tag> tags = taggable.getTags();
		return evaluateTags(tags);
	}

	/**
	 * Validate that the Taggable object remains consistent for the given operation.
	 * 
	 * @return true iff the Taggable object is consistent for the operation.
	 */
	public boolean validateOperation(Set<Tag> existingTags, TaggableOperation operation, Set<Tag> tagsToAddOrRemove) {
		if (operation == TaggableOperation.CREATE) {
			return evaluateTags(tagsToAddOrRemove);
		}
		// If the rule was violated before the operation, then we should not blame this
		// operation for the inconsistency.
		if (!evaluateTags(existingTags)) {
			return true;
		}

		// Validate the changeset on a copy to avoid mutating the entity's tags.
		Set<Tag> tagsCopy = new HashSet<>(existingTags);
		switch (operation) {
		case ADD_TAG:
			tagsCopy.addAll(tagsToAddOrRemove);
			break;
		case REMOVE_TAG:
			tagsCopy.removeAll(tagsToAddOrRemove);
			break;
		case CREATE:
			throw new IllegalStateException("This is unreachable since the create operation is handled above.");
		}

		return evaluateTags(tagsCopy);
	}

	/**
	 * Fix the Taggable object so that it becomes consistent.
	 * 
	 * @throws ConsistencyRuleViolationException if the Taggable object is already
	 *                                           in a consistent state.
	 * @return true iff the fix was successful.
	 */
	public abstract boolean fix(Taggable taggable)
			throws ConsistencyFixerViolationException, ConsistencyRuleViolationException;

	/**
	 * Evaluate the set of tags for consistency.
	 * 
	 * @return true iff the set of tags is consistent.
	 */
	protected abstract boolean evaluateTags(Set<Tag> tags);

	/**
	 * @return a descriptive message to be shown when this consistency rule is
	 *         violated.
	 */
	public abstract String getViolationMessage();

	/**
	 * @return the tags that this rule checks.
	 */
	public Set<Tag> getTags() {
		return this.tags;
	}
}
