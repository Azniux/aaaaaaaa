package models;

import java.util.*;
import javax.persistence.*;

import io.ebean.*;
import play.data.format.*;
import play.data.validation.*;


@Entity
public class Project extends Model {

    @Id
    private Long id;

    @Constraints.Required
    private String name;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Employee> employees;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Employee> getEmployee() {
        return employees;
    }

    public void Employees(List<Employee> employees) {
        this.employees = employees;
    }

    public static List<Project> findAll() {
        return Project.find.query().where().orderBy("name asc").findList();
    }

    public static Finder<Long, Project> find = new Finder<Long, Project>(Project.class);

    public static Map<String, String> options() {
        LinkedHashMap<String, String> options = new LinkedHashMap();

        for (Project c: Project.findAll()) {
            options.put(c.getId().toString(), c.getName());
        }
        
        return options;
    }

}