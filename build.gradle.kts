
import myaa.subkt.ass.*
import myaa.subkt.tasks.*
import myaa.subkt.tasks.Mux.*
import myaa.subkt.tasks.Nyaa.*
import java.awt.Color
import java.time.*

plugins {
	id("myaa.subkt")
}

subs {
	fun Task.season(): String {
		if (propertyExists("season")) {
			return get("season").get()
		} else {
			return batch.substringAfter("book")
		}
	}

	fun Task.ep(): String {
		return get("ep").get()
	}

	readProperties("sub.properties")
	episodes(getList("episodes"))
	batches(getMap("seasons", "episodes"))

	fun Task.episodeTitle(tags: String): String {
		return "Infinity Train S0${season()}E${ep()} - ${getRaw("title")} (${tags}) [0x539]"
	}

	fun Task.seasonTitle(tags: String): String {
		return "Infinity Train Book ${getRaw("numeral")} - ${getRaw("title")} (S0${season()}) ($tags) [0x539]"
	}

	val mks by task<Mux> {
		from("${season()}/${ep()}.ass")

		attach("fonts", "fonts/${episode}") {
			includeExtensions("ttf", "otf")
		}

		onFaux(ErrorMode.FAIL)

		val name = episodeTitle("SUBS")
		out("${season()}/mux/${name}.mks")
	}

	val mkv by task<Mux> {
		from("${season()}/raw/${ep()}.mkv") {
			tracks {
				include(track.type != TrackType.SUBTITLES)
			}
		}

		from(mks.item())

		val name = episodeTitle(getRaw("book${season()}.src") + " 1080p")
		out("${season()}/mux/${name}.mkv")
	}

	batchtasks {
		val mks_torrent by task<Torrent> {
			from(mks.batchItems())
			val name = seasonTitle("SUBS")
			into(name)
			out(name + ".torrent")
		}

		val mkv_torrent by task<Torrent> {
			from(mkv.batchItems())
			val name = seasonTitle(getRaw("src") + " 1080p")
			into(name)
			out(name + ".torrent")
		}
	}
}
