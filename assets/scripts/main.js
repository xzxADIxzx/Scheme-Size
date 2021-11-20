require("Scheme-Size/mod");

Events.on(EventType.ClientLoadEvent, event => {
	// don`t check for updates
	if(!Core.settings.getBool("checkupdate")) return;

	var verion = Vars.mods.locateMod("scheme-size").meta.version;
	Http.get("https://api.github.com/repos/xzxADIxzx/Scheme-Size/releases", res => {
		var str = res.getResultAsString();
		var json = JSON.parse(str);

		if(json[0].tag_name.slice(1) != verion){
			Vars.ui.showInfo("@updater.info")
		}
	}, () => {});
});

// why not
Vars.enableConsole = true;