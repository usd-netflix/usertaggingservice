package tagging;

/**
 * A tag represents some property on a Taggable entity.
 */
public class Tag {
	
	private String id;
	private String name;
	
	public Tag(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public static Tag create(String tagName) {
		UserTagDatabase udb = new UserTagDatabase();
		Tag tag = udb.insertTag(tagName);
		udb.close();
		return tag;
	}
	
	public static Tag fromName(String tagName) {
		UserTagDatabase udb = new UserTagDatabase();
		Tag tag = udb.getTag(tagName);
		udb.close();
		return tag;
	}
	
	public String getID() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	// Ensure that tags with the same name aren't duplicated in a Set.
	@Override
    public boolean equals(Object to_compare){
        if( !(to_compare instanceof Tag))
            return false;
        Tag other = (Tag) to_compare;
        return this.getName().equals(other.getName());
    }
	
	@Override
    public int hashCode() {
        return this.getName().hashCode();
    }
	
	@Override
	public String toString() {
		return this.getName();
	}
}
