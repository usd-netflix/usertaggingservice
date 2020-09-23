package tagging.consistency;

import java.util.HashSet;
import java.util.Set;

import tagging.Tag;
import tagging.Taggable;

/**
 * Models the NAND logic for two or more tags:
 * 
 * Evaluates as invalid when all tags are present on the Taggable object, and
 * valid in all other circumstances.
 */
public class NANDConsistencyRule extends ConsistencyRule {

	public NANDConsistencyRule(Tag tagA, Tag tagB, Tag... tags) {
		super(tagA, tagB, tags);
	}

	@Override
	public boolean fix(Taggable taggable) throws ConsistencyRuleViolationException, ConsistencyFixerViolationException {
		if (this.evaluate(taggable)) {
			throw new ConsistencyFixerViolationException(this);
		}
		// We remove a random tag to fix, though theoretically we could run
		// arbitrary business logic to accomplish a specific goal here.
		Tag randomTag = this.tags.iterator().next();
		HashSet<Tag> randomTagSet = new HashSet<>();
		randomTagSet.add(randomTag);
		return taggable.removeTags(randomTagSet);
	}

	@Override
	protected boolean evaluateTags(Set<Tag> tags) {
		return !tags.containsAll(this.tags);
	}

	@Override
	public String getViolationMessage() {
		return String.format("Cannot have all these tags %s present on this object at once.", this.tags);
	}
}
