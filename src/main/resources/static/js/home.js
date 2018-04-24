document.querySelectorAll('.delete').forEach(function(element) {
	element.addEventListener('click', function(event) {
		fetch(event.target.dataset.deleteUrl, {
			method: 'delete',
			credentials: 'include'
		}).then(response =>
			response.text().then(html => {
				document.getElementById('streamers').outerHTML = html;
			})
		);
	});
});
