package controllers;

import play.mvc.*;
import play.mvc.Http.*;
import play.mvc.Http.MultipartFormData.FilePart;
import java.io.File;

import play.api.Environment;
import play.data.*;
import play.db.ebean.Transactional;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;

import models.*;
import models.users.*;
import views.html.*;


/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    private FormFactory formFactory;

    private Environment e;
    
        @Inject
        public HomeController(FormFactory f, Environment env) {
            this.formFactory = f;
            this.e= env;
        }
    
        public Result index(Long cat) {
            List<Employee> employeeList = null;
            List<Department> departmentList = Department.findAll();
            if (cat == 0) {
                employeeList = Employee.findAll();
            }
            else {
                employeeList = Department.find.ref(cat).getEmployees();
            }
            return ok(index.render(employeeList, departmentList, User.getUserById(session().get("email")), e));
        }

    public Result customer() {
        List<Customer> customerList = Customer.findAll();
        return ok(customer.render(customerList,User.getUserById(session().get("email"))));
    }
    @Security.Authenticated(Secured.class)
    @With(AuthAdmin.class)
    public Result addEmployee() {
        Form<Employee> employeeForm = formFactory.form(Employee.class);
        return ok(addEmployee.render(employeeForm, User.getUserById(session().get("email"))));
    }

    public String saveFile(Long id, FilePart<File> uploaded){
        String mimeType = uploaded.getContentType();
        if(uploaded != null) {
            if(mimeType.startsWith("image/")) {
                String fileName = uploaded.getFilename();
                File file = uploaded.getFile();
                IMOperation op = new IMOperation();
                op.addImage(file.getAbsolutePath());
                op.resize(300,300);
                op.addImage("public/images/employeeImages/"+id+".jpg");
                IMOperation thumb = new IMOperation();
                thumb.addImage(file.getAbsolutePath());
                thumb.resize(60);
                thumb.addImage("public/images/employeeImages/thumbnails/"+id+".jpg");
                
                File dir = new File("public/images/employeeImages/thumbnails/");
                if(!dir.exists()){
                    dir.mkdirs();
                }
                ConvertCmd cmd = new ConvertCmd();

                try{
                    cmd.run(op);
                    cmd.run(thumb);
                } catch (Exception e){
                    e.printStackTrace();
                }
                return " and image saved";
            }
        }
        return "/ no file";
    }
    

    public Result addEmployeeSubmit() {
        Employee newEmployee;
        Form<Employee> newEmployeeForm = formFactory.form(Employee.class).bindFromRequest();

        if (newEmployeeForm.hasErrors()) {
            return badRequest(addEmployee.render(newEmployeeForm, User.getUserById(session().get("email"))));
        }
        else {
            newEmployee = newEmployeeForm.get();
            

            if (newEmployee.getId() == null) {
                newEmployee.save(); 
                
            
            
            for(Long cat : newEmployee.getProjSelect()) {
                newEmployee.projects.add(Project.find.byId(cat));
            
            
        }
        newEmployee.update();
        flash("success", "Employee " + newEmployee.getName() + " was updated");    
            
        }
    }
        
        MultipartFormData data = request().body().asMultipartFormData();
        FilePart<File> image = data.getFile("upload");

        String saveImageMsg = saveFile(newEmployee.getId(), image);
        flash("success", "Employee " + newEmployee.getName() + " has been created/updated " + saveImageMsg);

        return redirect(controllers.routes.HomeController.index(0));
    }
    @Security.Authenticated(Secured.class)
    @With(AuthAdmin.class)
    @Transactional
    public Result addCustomer() {
        Form<Customer> customerForm = formFactory.form(Customer.class);
        return ok(addCustomer.render(customerForm,User.getUserById(session().get("email"))));
    }

    public Result addCustomerSubmit() {
        Form<Customer> newCustomerForm = formFactory.form(Customer.class).bindFromRequest();
        

        if (newCustomerForm.hasErrors()) {
            return badRequest(addCustomer.render(newCustomerForm,User.getUserById(session().get("email"))));
            
        } 
        else {
            Customer newCustomer = newCustomerForm.get();
            
            if (newCustomer.getId() == null) {
                newCustomer.save();
                flash("success", "Customer " + newCustomer.getName() + " was added");                
            }

            else {
                newCustomer.update();
                flash("success", "Customer " + newCustomer.getName() + " was updated");                
            }



            return redirect(controllers.routes.HomeController.customer());
        }
    }
    @Security.Authenticated(Secured.class)
    @With(AuthAdmin.class)
    @Transactional
    public Result deleteEmployee(Long id) {
        Employee.find.ref(id).delete();

        flash("success", "Employee has been deleted");
        
        return redirect(routes.HomeController.index(0));
    }
    public Result deleteCustomer(Long id) {
        Customer.find.ref(id).delete();
        flash("success", "Customer has been deleted");

        return redirect(routes.HomeController.index(0));
    }

    @Security.Authenticated(Secured.class)
    @With(AuthAdmin.class)
    @Transactional
    public Result updateEmployee(Long id) {
        Employee p;
        Form<Employee> employeeForm;

        try {
            p = Employee.find.byId(id);
            employeeForm = formFactory.form(Employee.class).fill(p);
        } 
        catch (Exception ex) {
            return badRequest("error");
        }
        return ok(updateEmployee.render(employeeForm,User.getUserById(session().get("email"))));
    }
    @Transactional
    public Result updateCustomer(Long id) {        
        Customer c;
        Form<Customer> customerForm;

        try {
            c = Customer.find.byId(id);
            customerForm = formFactory.form(Customer.class).fill(c);
        }
        catch (Exception ex) {
            return badRequest("error");
        }

        return ok(addCustomer.render(customerForm,User.getUserById(session().get("email"))));
    }

    public Result updateEmployeeSubmit(Long id) {
        
        // Retrieve the submitted form object (bind from the HTTP request)
        Form<Employee> updateEmployeeForm = formFactory.form(Employee.class).bindFromRequest();

        // Check for errors (based on constraints set in the Employee class)
        if (updateEmployeeForm.hasErrors()) {
            // Display the form again by returning a bad request
            return badRequest(updateEmployee.render(id,updateEmployeeForm, User.getUserById(session().get("email"))));
        } else {
            // No errors found - extract the Employee detail from the form
            Employee e = updateEmployeeForm.get();
            e.setId(id);

            List<Category> newProj = new ArrayList<Category>();
            for(Long cat : e.getProjSelect()){
                newProj.add(Category.find.byId(cat));
            }
            e.categories = newProj;
            
            
            
            //update (save) this Employee
            e.update();

            MultipartFormData data = request().body().asMultipartFormData();
            FilePart<File> image = data.getFile("upload");

            String saveImageMsg = saveFile(e.getId(), image);
            if(saveImageMsg.equals("/ no file")){
                flash("success", "Employee " + e.getName() + " has been  updated ");
                
            } else {
                flash("success", "Employee " + e.getName() + " has been  updated " + saveImageMsg);
            }
            
            // Redirect to the index page
            return redirect(controllers.routes.HomeController.index(0));
        }
    }

}
