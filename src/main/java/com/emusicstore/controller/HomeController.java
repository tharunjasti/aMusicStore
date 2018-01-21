package com.emusicstore.controller;

import com.emusicstore.dao.ProductDao;
import com.emusicstore.model.Product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;


@Controller
public class HomeController {
	
	private Path path;
	
	@Autowired
    private ProductDao productDao;

    @RequestMapping("/")
    public String home() {
        return "home";
    }


    @RequestMapping("/productList")
    public String getProducts(Model model) {
        List<Product> productList = productDao.getAllProducts();
        model.addAttribute("products",productList);

        return "productList";
    }
    
    @RequestMapping("productList/viewProduct/{productId}")
    public String viewProduct(@PathVariable("productId") int productId, Model model) throws IOException{
    	Product product=productDao.getProductById(productId);
    	model.addAttribute(product);
    	return "viewProduct";
    }
    
    @RequestMapping("/admin")
    public String admin(){
    	return "admin";
    }
    
    @RequestMapping("/admin/productInventory")
    public String productInventory(Model model){
    	List<Product> products=productDao.getAllProducts();
    	model.addAttribute("products",products);
    	return "productInventory";	
    }
    
    @RequestMapping("/admin/productInventory/addProduct")
    public String addProduct(Model model) {
    	
    	Product product = new Product();
        product.setProductCategory("instrument");
        product.setProductCondition("new");
        product.setProductStatus("active");
        
        System.out.println("add prodect get method is called and valriables are::"+product);

        model.addAttribute("product", product);
        return "addProduct";
    }
    
    @RequestMapping(value = "/admin/productInventory/addProduct", method = RequestMethod.POST)
    public String addProductPost(@Valid @ModelAttribute("product") Product product,BindingResult result, HttpServletRequest request) {
       
    	if(result.hasErrors()){
    		return "addProduct";
    	}
    	productDao.addProduct(product);
        
        MultipartFile productImage=product.getProductImage();
        String rootDirectory=request.getSession().getServletContext().getRealPath("/");
        path = Paths.get(rootDirectory + "\\WEB-INF\\resources\\images\\"+product.getProductId()+".png");
        System.out.println("path for image is::"+path);
        if (productImage != null && !productImage.isEmpty()) {
        	 try {
                 productImage.transferTo(new File(path.toString()));
             } catch (Exception e) {
                 e.printStackTrace();
                 throw new RuntimeException("Product image saving failed", e);
             }
        	
        }

        return "redirect:/admin/productInventory";
    }
    
    @RequestMapping("admin/productInventory/deleteProduct/{productId}")
    public String deleteProduct(@PathVariable("productId") int id, Model model, HttpServletRequest request){
    	
    	String rootDirectory = request.getSession().getServletContext().getRealPath("/");
        path = Paths.get(rootDirectory + "\\WEB-INF\\resources\\images\\"+id+".png");

        if (Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    	
    	productDao.deleteProduct(id);
    	
    	return "redirect:/admin/productInventory";
    }
        
    @RequestMapping("/admin/productInventory/editProduct/{id}")
    public String editProduct(@PathVariable("id") int id, Model model) {
        Product product = productDao.getProductById(id);
        model.addAttribute(product);

        return "editProduct";
    }
    
    
    
    @RequestMapping(value = "/admin/productInventory/editProduct", method = RequestMethod.POST)
    public String editProduct(@Valid @ModelAttribute("product") Product product, BindingResult result, Model model, HttpServletRequest request) {

        if (result.hasErrors()) {
            return "editProduct";
        }
        productDao.editProduct(product);

        MultipartFile productImage = product.getProductImage();
        String rootDirectory = request.getSession().getServletContext().getRealPath("/");
        path = Paths.get(rootDirectory + "\\WEB-INF\\resources\\images\\"+product.getProductId()+".png");

        if (productImage != null && !productImage.isEmpty()) {
            try {
                productImage.transferTo(new File(path.toString()));
            } catch (Exception e) {
                throw new RuntimeException("Product image saving failed" , e);
            }
        }

        return "redirect:/admin/productInventory";
    }
    

}
