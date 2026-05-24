// the script is mainly used to help with development of the project
// it simply loads a few classes into the dev console

var mod = Vars.mods.getMod("schema")
var get = Vars.mobile ? (pkg) => null : (pkg) => mod.loader.loadClass(pkg).newInstance()

const Main    = mod.main
const Updater = get("schema.Updater")
