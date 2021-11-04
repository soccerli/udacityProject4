package com.example.demo.controllers;

import java.util.Optional;

import com.example.demo.exception.UserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {
	final private Logger logger= LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@GetMapping("/id/{id}")
	public ResponseEntity<User> findById(@PathVariable Long id) {
		return ResponseEntity.of(userRepository.findById(id));
	}
	
	@GetMapping("/{username}")
	public ResponseEntity<User> findByUserName(@PathVariable String username) {
		User user = userRepository.findByUsername(username);
		return user == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
	}
	
	@PostMapping("/create")
	public ResponseEntity<User> createUser(@RequestBody CreateUserRequest createUserRequest){

		if(!createUserRequest.verifyPasswd()){
			logger.error("createUser = \"error-Passwd not enough length or passwd not equal to confirmed passwd\"");
			return ResponseEntity.badRequest().build();
		}

		try{
			User user = new User();
			user.setUsername(createUserRequest.getUsername());
			user.setPassword(bCryptPasswordEncoder.encode(createUserRequest.getPassword()));

			Cart cart = new Cart();

			//this is needed for persistent
			cart.setUser(user);
			user.setCart(cart);

			userRepository.save(user);
			cartRepository.save(cart);
			//user.setCart(cart);
			//userRepository.save(user);
			logger.debug("createUser = success");
			return ResponseEntity.ok(user);
		}catch (Exception e){
			throw new UserException(e.getMessage());
		}
	}
	
}