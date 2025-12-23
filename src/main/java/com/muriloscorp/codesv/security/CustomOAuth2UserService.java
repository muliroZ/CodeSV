package com.muriloscorp.codesv.security;

import com.muriloscorp.codesv.model.User;
import com.muriloscorp.codesv.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        Long githubId = ((Number) attributes.get("id")).longValue();
        String login = ((String) attributes.get("login"));
        String name = ((String) attributes.get("name"));
        String avatarUrl = ((String) attributes.get("avatar_url"));

        User user = userRepository.findByGithubId(githubId)
                .orElse(new User());

        user.setGithubId(githubId);
        user.setLogin(login);
        user.setName(name != null ? name : login);
        user.setAvatarUrl(avatarUrl);

        userRepository.save(user);

        return oAuth2User;
    }
}
