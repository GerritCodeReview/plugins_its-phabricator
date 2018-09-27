public class ManiphestSearch {
  private int id;
  private JsonElement fields;
  private Attachments attachments;

  public int getId() {
    return id;
  }

  public JsonElement getFields() {
    return fields;
  }

  public Attachments getAttachments() {
    return attachments;
  }

  public class Attachments {
    private Projects projects;

    public Projects getProjects() {
      return projects;
    }
  }

  public class Projects {
    private JsonElement projectPHIDs;

    public JsonElement getProjectPHIDs() {
      return projectPHIDs;
    }
  }
}
