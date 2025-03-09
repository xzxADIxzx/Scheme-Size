// this script is mainly used to help with development of the mod
// as it loads the main classes into the dev console

var mod = Vars.mods.getMod("schema")
var get = (pkg) => mod.loader.loadClass(pkg).newInstance()

// loader is null on mobile devices, because dex is used instead of java byte code
if (Vars.mobile) get = (pkg) => null;

const Main = mod.main
const Updater = get("schema.Updater")
const ServerIntegration = get("scheme.ServerIntegration")
const ClajIntegration = get("scheme.ClajIntegration")
const Stl = get("schema.ui.Style")

// for some unknown reason, this works only here, in the script
// basically, a new atlas region is created to be overridden then by the sprite from the override directory
Core.atlas.addRegion("status-invincible", Core.atlas.white());
