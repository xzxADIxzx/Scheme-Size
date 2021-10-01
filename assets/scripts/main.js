require("Scheme-Size/mod");

Events.on(EventType.ClientLoadEvent, e => {
	// delete old interface... idk why but does work in java?
	Time.runTask(10, () => {
		Vars.ui.hudGroup.children.get(5).clear();
	});

	// don`t check for updates
	if(!Core.settings.getBool("checkupdate")) return;

	var ver = Vars.mods.locateMod("scheme-size").meta.version;
	Http.get("https://api.github.com/repos/xzxADIxzx/Scheme-Size/releases", res => {
		var str = res.getResultAsString();
		var json = JSON.parse(str);

		if(json[0].tag_name.slice(1) != ver){
			Vars.ui.showInfo("@updater.info")
		}
	});
});

// why not
Vars.enableConsole = true;