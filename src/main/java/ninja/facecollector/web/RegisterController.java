package ninja.facecollector.web;

import lombok.Data;
import ninja.facecollector.services.CollectService;
import ninja.facecollector.services.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Controller
public class RegisterController {
	private CollectService collectService;
	private RegisterService registerService;

	@Autowired
	public RegisterController(CollectService collectService, RegisterService registerService) {
		this.collectService = collectService;
		this.registerService = registerService;
	}

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("request", new CollectRequest());

		return "home";
	}

	@PostMapping("/collect")
	public String collect(@Valid @ModelAttribute("request") CollectRequest request, BindingResult bindingResult) {
		String view;

		if (bindingResult.hasErrors()) {
			view = "home";
		} else {
			view = String.format("redirect:%s", registerService.authorizeFaceCollector(request.getStreamer()));
		}

		return view;
	}

	@GetMapping("/joined")
	@ResponseStatus(HttpStatus.OK)
	public String joined(@RequestParam("state") String registrationId, @RequestParam("guild_id") String guildId) {
		return registerService.findStreamerByRegistrationId(registrationId).map(streamer -> {
			registerService.registerGuildId(registrationId, guildId);

			collectService.collect(streamer, guildId);

			return "success";
		}).orElseThrow(() -> new RuntimeException("No registration found"));
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	@Data
	private static class CollectRequest {
		@NotNull
		private String streamer;
	}
}
