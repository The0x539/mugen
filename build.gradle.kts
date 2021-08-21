
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
	readProperties("sub.properties")
	episodes(getList("episodes"))
	batches(getMap("seasons", "episodes"))

	fun Task.getSeason(): String {
		return episode.substringBefore('x')
	}

	fun Task.getBatchSeason(): String {
		return batch.substringAfter("book")
	}

	fun Task.getEp(): String {
		return episode.substringAfter('x')
	}

	fun Task.formatEpisode(tags: String): String {
		return "Infinity Train S0${getSeason()}E${getEp()} - ${getRaw("title")} (${tags}) [0x539]"
	}

	fun Task.formatSeason(tags: String): String {
		return "Infinity Train Book ${getRaw("numeral")} - ${getRaw("title")} (S0${getBatchSeason()}) ($tags) [0x539]"
	}

	val mks by task<Mux> {
		from("${getSeason()}/${getEp()}.ass")

		attach("fonts", "fonts/${episode}") {
			includeExtensions("ttf", "otf")
		}

		onFaux(ErrorMode.FAIL)

		val name = formatEpisode("SUBS")
		out("${getSeason()}/mux/${name}.mks")
	}

	val mkv by task<Mux> {
		val season = getSeason()
		from("${getSeason()}/raw/${getEp()}.mkv") {
			tracks {
				include(track.type != TrackType.SUBTITLES)
			}
		}

		from(mks.item())

		val name = formatEpisode(getRaw("book${getSeason()}.src") + " 1080p")
		out("${getSeason()}/mux/${name}.mkv")
	}

	batchtasks {
		val mks_torrent by task<Torrent> {
			from(mks.batchItems())
			val name = formatSeason("SUBS")
			into(name)
			out(name + ".torrent")
		}

		val mkv_torrent by task<Torrent> {
			from(mkv.batchItems())
			val name = formatSeason(getRaw("src") + " 1080p")
			into(name)
			out(name + ".torrent")
		}
	}
}
