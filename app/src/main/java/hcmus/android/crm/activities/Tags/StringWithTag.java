package hcmus.android.crm.activities.Tags;

public class StringWithTag {
    private String string;
    private String tagId;

    public StringWithTag(String string, String tagId) {
        this.string = string;
        this.tagId = tagId;
    }

    public String getString() {
        return string;
    }

    public String getTagId() {
        return tagId;
    }

    @Override
    public String toString() {
        return string;
    }
}
