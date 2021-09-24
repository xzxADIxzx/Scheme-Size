require("mod");

// don`t check for updates
if(!Core.settings.getBool("checkmodupdate")) return;

Events.on(EventType.ClientLoadEvent, e => {
	var ver = Vars.mods.locateMod("scheme-size").meta.version;
	Http.get("https://api.github.com/repos/xzxADIxzx/Scheme-Size/tags", res => {
		var str = res.getResultAsString();
		var json = JSON.parse(str);

		if(json[0].name.slice(1) != ver){
			var dialog = new BaseDialog("@updater.name");
			dialog.labelWrap("@updater.info").row();
			dialog.cont.button("@ok", () => dialog.hide()).size(100, 50);
			dialog.show();
		}
	});
});