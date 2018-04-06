package ninja.facecollector.web;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ninja.facecollector.services.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;

@Slf4j
@Controller
public class RegisterController {
	private RegisterService registerService;

	@Autowired
	public RegisterController(RegisterService registerService) {
		this.registerService = registerService;
	}

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("request", new CollectRequest());

		return "home";
	}

	@PostMapping("/collect")
	public String collect(@Valid @ModelAttribute("request") CollectRequest request) {
		return String.format("redirect:%s", registerService.authorizeFaceCollector(request.getStreamer()));
	}

	@GetMapping("/joined")
	@ResponseStatus(HttpStatus.OK)
	public String joined(@RequestParam("state") String registrationId, @RequestParam("guild_id") String guildId) {

		registerService.registerGuildId(registrationId, guildId);

		return "success";
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	@Data
	private static class CollectRequest {
		private String streamer;
	}
}
