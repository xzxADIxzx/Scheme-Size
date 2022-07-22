// well, after the 136th build, it became much easier
Vars.maxSchematicSize = 511;

// it is really useful for development
var mod = Vars.mods.getMod("scheme-size-new")
var get = (pkg) => mod.loader.loadClass(pkg).newInstance()

const SchemeMain = mod.main
const SchemeVars = get("scheme.SchemeVars")
const SchemeUpdater = get("scheme.SchemeUpdater")
const Backdoor = get("scheme.Backdoor")