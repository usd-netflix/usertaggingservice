package tagging.consistency;

import java.util.Set;

import tagging.Tag;
import tagging.Taggable;

/**
 * Models the AND logic for two or more tags:
 * 
 * Evaluates as valid when all tags are present on the Taggable object, and
 * invalid in all other circumstances.
 */
public class ANDConsistencyRule extends ConsistencyRule {

	public ANDConsistencyRule(Tag tagA, Tag tagB, Tag ... tags) {
		super(tagA, tagB, tags);
	}

	@Override
	public boolean fix(Taggable taggable) throws ConsistencyFixerViolationException, ConsistencyRuleViolationException {
		if (this.evaluate(taggable)) {
			throw new ConsistencyFixerViolationException(this);
		}
		// Simply add all tags to satisfy the AND constraint and fix.
		return taggable.addTags(this.tags);
	}

	@Override
	protected boolean evaluateTags(Set<Tag> tags) {
		return tags.containsAll(this.tags);
	}

	@Override
	public String getViolationMessage() {
		return String.format("Must have all these tags %s present on this object.", this.tags);
	}
}
