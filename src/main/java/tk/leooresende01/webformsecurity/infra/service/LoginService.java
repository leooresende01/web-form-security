package tk.leooresende01.webformsecurity.infra.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import tk.leooresende01.webformsecurity.infra.controller.dto.AutenticacaoForm;
import tk.leooresende01.webformsecurity.infra.util.CryptoUtil;

@Service
public class LoginService {
	
	private static final String AES_HEADER = "Aes-Crypt";
	
	public AutenticacaoForm getAuthFormFromCrypto(HttpServletRequest req, String payload) {
		//Pegando a chave privada que foi salva na sessão do usuario
		Object chave = req.getSession().getAttribute(HomeService.PRIVATE_KEY_NAME);
		
		//Descriptografando o corpo da requisição em AES
		String cryptBase64 = CryptoUtil.descryptAESWhitPrivateKey(chave.toString(), payload);
		
		//Descriptografando a base64 e retornando o objeto de autenticação
		AutenticacaoForm autenticacaoForm = CryptoUtil.decodeBase64AndGetAuth(cryptBase64);
		return autenticacaoForm;
	}

	public ResponseEntity<?> autenticarUsuario(AuthenticationManager authManager, AutenticacaoForm authForm) {
		//Verificando se as credenciais existem no banco de dados
		Authentication auth = new UsernamePasswordAuthenticationToken(authForm.getUsername(), authForm.getPassword());
		Authentication authenticate = authManager.authenticate(auth);
		if(authenticate.isAuthenticated()) {
			//Autenticando o usuario
			SecurityContextHolder.getContext().setAuthentication(authenticate);
			return ResponseEntity.ok().build();
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}
	
	public void verificarTime(AutenticacaoForm authForm, HttpServletRequest req) {
		//Verificar se o parametro do cabeçalho AES_Crypt equivale a o do corpo da requisição
		String aesTimeCrypt = req.getHeader(AES_HEADER);
		Object chave = req.getSession().getAttribute(HomeService.PRIVATE_KEY_NAME);
		String timeDescrypt = CryptoUtil.descryptAESWhitPrivateKey(chave.toString(), aesTimeCrypt);
		if (!authForm.getTime().equals(timeDescrypt)) {
			throw new IllegalArgumentException();
		}
	}
}