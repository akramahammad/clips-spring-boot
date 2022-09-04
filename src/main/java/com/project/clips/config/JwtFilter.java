package com.project.clips.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;

@Component
public class JwtFilter extends OncePerRequestFilter {

	private static final Logger logger=LoggerFactory.getLogger(JwtFilter.class);
	
	@Autowired
	private JwtTokenUtil jwtUtil;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		logger.info("Starting Jwt Filter");
		
		String reqToken=request.getHeader("Authorization");
		logger.info("Request header ::{}",reqToken);
		if(reqToken!=null && reqToken.startsWith("Bearer ")) {
			
			String token=reqToken.substring(7);
			String email=null;
			
			try {
				email=jwtUtil.getUsernameFromToken(token);
				
			}
			catch (IllegalArgumentException e) {
				logger.warn("Unable to get JWT Token");
			} catch (ExpiredJwtException e) {
				logger.warn("JWT Token has expired");
			}

			if(email!=null && SecurityContextHolder.getContext().getAuthentication()==null) {

				UserDetails userDetails=userDetailsService.loadUserByUsername(email);
				
				if(jwtUtil.validateToken(token, userDetails)) {
					
					UsernamePasswordAuthenticationToken authToken= 
							new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null, userDetails.getAuthorities());
					
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}
		}
		
		
		
		logger.info("Ending Jwt Filter");
		filterChain.doFilter(request, response);

	}

}
