VF = Vex.Flow;

const MAX_NOTES = 10;

var width         = window.innerWidth  - 10;
var height        = window.innerHeight - 30;
var x             = 0;
var y             = height / 5;
var num_beats     = ScoreView.getNumBeats();
var beat_value    = ScoreView.getBeatValue();
var clef          = ScoreView.getClef();

var exercise_size = ScoreView.getExerciseSize();

if (exercise_size > MAX_NOTES) // wide width preventing overlapping notes
    width += 15 * (exercise_size - MAX_NOTES);

var div = document.getElementById("ScoreView")
var renderer = new VF.Renderer(div, VF.Renderer.Backends.SVG); // Specify SVG Renderer

renderer.resize(width, height);

var context = renderer.getContext();
var stave = new VF.Stave(x, y, width - 10);

stave.addClef(clef) // Specify Clef for stave
stave.setContext(context).draw();

var notes = [];
for (i = 0; i < exercise_size; i++)
{
    if (!ScoreView.isBlackKey(i))
        notes.push(new VF.StaveNote({clef: clef, keys: [ScoreView.getTone(i)], duration: ScoreView.getDuration(i)}));
    else
        notes.push(new VF.StaveNote({clef: clef, keys: [ScoreView.getTone(i)], duration: ScoreView.getDuration(i)})
            .addAccidental(0, new VF.Accidental("#")));;
}

var voice = new VF.Voice({num_beats: num_beats,  beat_value: beat_value});
voice.addTickables(notes);

var formatter = new VF.Formatter().joinVoices([voice]).format([voice], width*0.9);
var beams = VF.Beam.generateBeams(notes);

Vex.Flow.Formatter.FormatAndDraw(context, stave, notes);
beams.forEach(function(b) {b.setContext(context).draw()})

voice.draw(context, stave);