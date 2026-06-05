// the script is mainly used to help with development of the project
// it simply loads a few classes into the dev console

var mod = Vars.mods.getMod("schema")
var get = Vars.mobile ? (pkg) => null : (pkg) => mod.loader.loadClass(pkg).newInstance()

const Main    = mod.main
const Tools   = get("schema.Tools")
const Updater = get("schema.Updater")

// temporarily load a dummy sprite to be overridden later
// this only works within the scope of the script
Core.atlas.addRegion("status-invincible", Core.atlas.white())
StatusEffects.shielded.uiIcon=StatusEffects.shielded.fullIcon
