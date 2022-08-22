import myaa.subkt.ass.*
import myaa.subkt.tasks.*
import myaa.subkt.tasks.Mux.*
import myaa.subkt.tasks.Nyaa.*
import java.awt.Color
import java.time.*

plugins {
	id("myaa.subkt")
}

fun readLinesFromFile(path: String): List<String> = File(path).readLines()

subs {
	fun SubTask.season(): String = if (isBatch) {
		batch.substringAfter("book")
	} else {
		episode.substringBefore('x')
	}

	val numerals = listOf("Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight")
	fun SubTask.numeral(): String = numerals[season().toInt()]

	fun Task.ep(): String = episode.substringAfter('x')

	readProperties("sub.properties")
	episodes(getList("episodes"))
	batches(getMap("seasons", "episodes"))

	fun SubTask.episodeTitle(tags: String? = null): String {
		val tagBlock = tags?.let { "($it) " } ?: ""
		return "Infinity Train S0${season()}E${ep()} - ${getRaw("title")} ${tagBlock}[0x539]"
	}

	fun SubTask.seasonTitle(tags: String): String {
		// For batch tasks, `entry` or `batch` would work.
		// For episode tasks, this is necessary.
		val book = "book${season()}"

		val title = get("${book}.title").get()
		val seasonNum = "S0" + season()

		return "Infinity Train Book ${numeral()} - ${title} (${seasonNum}) ($tags) [0x539]"
	}

	merge {
		from("${season()}/${ep()}.ass") {
			scriptInfo {
				title = episodeTitle()
				timing = "The0x539"
			}
			includeExtraData(true)
			includeProjectGarbage(false)
			removeComments(false)
		}
	}

	val mks by task<Mux> {
		from(merge.item()) {
			tracks {
				name(episodeTitle())
				lang("eng")
				default(true)
				forced(false)
			}
		}

		attach("fonts", "fonts/${episode}") {
			includeExtensions("ttf", "otf")
		}
		
		mimeTypes["otf"] = "application/x-truetype-font"

		onFaux(ErrorMode.FAIL)

		title(episodeTitle())
		out("${season()}/mux/${seasonTitle("SUBS")}/${episodeTitle("SUBS")}.mks")
	}

	val mkv by task<Mux> {
		from("${season()}/raw/${ep()}.mkv") {
			tracks {
				include(track.type != TrackType.SUBTITLES)
			}
		}

		from(mks.item())

		title(episodeTitle())
		val tags = getRaw("book${season()}.src") + " 1080p"
		out("${season()}/mux/${seasonTitle(tags)}/${episodeTitle(tags)}.mkv")
	}

	batchtasks {
		fun Torrent.configure(group: TaskGroup<Mux>, tags: String) {
			from(group.batchItems())
			val name = seasonTitle(tags)
			into(name)
			createdBy("SubKt")
			out(name + ".torrent")
			trackers(readLinesFromFile("../trackers.txt"))
		}

		val mks_torrent by task<Torrent> {
			configure(mks, "SUBS")
			pieceLength(64 * 1024)
		}

		val mkv_torrent by task<Torrent> {
			configure(mkv, getRaw("src") + " 1080p")
			pieceLength(4 * 1024 * 1024)
		}
	}
}
