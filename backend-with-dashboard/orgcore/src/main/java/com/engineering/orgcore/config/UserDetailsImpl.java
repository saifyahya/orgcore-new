package com.engineering.orgcore.config;

import com.engineering.orgcore.entity.Users;
import com.engineering.orgcore.enums.RoleEnum;
import com.engineering.orgcore.repository.UsersRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserDetailsImpl implements UserDetailsService {

    private UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = usersRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getIsActive() != 1) {
            throw new LockedException(username);
        }

        List<GrantedAuthority> grantedAuthorities = List.of(
                () -> RoleEnum.toValue(user.getRole().getRoleName())
        );
        return new User(user.getEmail(), user.getPassword(), grantedAuthorities);
    }
}
