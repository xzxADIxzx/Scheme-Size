require("mod");

// don`t check for updates
if(!Core.settings.getBool("checkupdate")) return;

Events.on(EventType.ClientLoadEvent, e => {
	var ver = Vars.mods.locateMod("scheme-size").meta.version;
	Http.get("https://api.github.com/repos/xzxADIxzx/Scheme-Size/tags", res => {
		var str = res.getResultAsString();
		var json = JSON.parse(str);

		if(json[0].name.slice(1) != ver){
			Vars.ui.showInfo("@updater.info")
		}
	});
});