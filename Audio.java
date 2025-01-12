import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Scanner;
import javax.sound.sampled.*;

class Song {
    String name;
    String filePath;

    public Song(String name, String filePath) {
        this.name = name;
        this.filePath = filePath;
    }
}

public class Audio {

    public static void main(String[] args) {
        LinkedList<Song> hipHopSongs = new LinkedList<>();
        hipHopSongs.add(new Song("Millionaire - Glory 320 Kbps", "C:\\Users\\KIIT0001\\Desktop\\java rporgams\\Millionaire - Glory 320 Kbps.wav"));
        hipHopSongs.add(new Song("Bake Baatein Peene Baad", "C:\\Users\\KIIT0001\\Desktop\\java rporgams\\new_192_Baaki Baatein Peene Baad - Arjun Kanungo.wav"));

        LinkedList<Song> sadSongs = new LinkedList<>();
        sadSongs.add(new Song("khairiyat", "C:\\Users\\KIIT0001\\Desktop\\java rporgams\\new_320_06 - Khairiyat (Happy) - Chhichhore (2019).wav"));
        sadSongs.add(new Song("khogaye", "C:\\Users\\KIIT0001\\Desktop\\java rporgams\\Kho Gaye (Mismatched 2) - (Raag.Fm).wav"));

        try (Scanner sc = new Scanner(System.in)) {
            String response = "N";

            while (!response.equals("Q")) {
                System.out.println("Select the type of song (1 for Hip-Hop, 2 for Sad Song, Q to Quit):");
                String songTypeChoice = sc.nextLine();

                if (songTypeChoice.equalsIgnoreCase("Q")) {
                    break;
                }

                LinkedList<Song> selectedSongs = null;

                switch (songTypeChoice) {
                    case "1":
                        selectedSongs = hipHopSongs;
                        break;
                    case "2":
                        selectedSongs = sadSongs;
                        break;
                    default:
                        System.out.println("Invalid choice");
                        continue;
                }

                playPlaylist(selectedSongs);

                System.out.println("Do you want to play the same playlist (S), explore another playlist (N), or quit (Q)?");
                response = sc.nextLine().toUpperCase();
            }
        }
    }

    // Function to play the playlist
    private static void playPlaylist(LinkedList<Song> songs) {
        ListIterator<Song> it = songs.listIterator();
        final Clip[] clip = new Clip[1];  // Use an array to hold the clip reference (final)

        if (it.hasNext()) {
            Song song = it.next();
            File file = new File(song.filePath);

            try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(file)) {
                clip[0] = AudioSystem.getClip();  // Initialize clip here
                clip[0].open(audioStream);  // Open the audio stream

                System.out.println("Now playing: " + song.name);
                clip[0].start();

                // Create a thread to control the playback
                new Thread(() -> {
                    Scanner sc = new Scanner(System.in);
                    String controlResponse = "";

                    while (!controlResponse.equals("Q")) {
                        System.out.println("\nControls: ");
                        System.out.println("Next Song (N), Quit (Q), +10 Seconds (+), -10 Seconds (-), Reset (R)");

                        controlResponse = sc.nextLine().toUpperCase();

                        switch (controlResponse) {
                            case "N": // Next song
                                clip[0].stop();  // Stop the current song
                                System.out.println("Moving to next song.");

                                // If there's another song in the playlist, play it
                                if (it.hasNext()) {
                                    Song nextSong = it.next();  // Move to the next song in the playlist
                                    File nextFile = new File(nextSong.filePath);

                                    // Stop and close the current clip before creating a new one
                                    clip[0].close();

                                    try (AudioInputStream nextAudioStream = AudioSystem.getAudioInputStream(nextFile)) {
                                        clip[0] = AudioSystem.getClip();  // Initialize a new clip for the next song
                                        clip[0].open(nextAudioStream);  // Open the next song's audio stream
                                        System.out.println("Now playing: " + nextSong.name);
                                        clip[0].start();  // Start the next song
                                    } catch (Exception e) {
                                        System.out.println("Error playing next song: " + e.getMessage());
                                    }
                                } else {
                                    System.out.println("End of playlist reached.");
                                }
                                break;
                            case "Q": // Quit
                                clip[0].stop();  // Stop the current song
                                clip[0].close();  // Close the clip
                                System.out.println("Exiting.");
                                break;
                            case "+": // +10 Seconds
                                int currentFrame = clip[0].getFramePosition();
                                int framesToAdd = (int)(10 * clip[0].getFormat().getFrameRate()); // 10 seconds in frames
                                clip[0].setFramePosition(currentFrame + framesToAdd);
                                System.out.println("Moved forward by 10 seconds.");
                                break;
                            case "-": // -10 Seconds
                                int currentFrameBack = clip[0].getFramePosition();
                                int framesToSubtract = (int)(10 * clip[0].getFormat().getFrameRate()); // 10 seconds in frames
                                clip[0].setFramePosition(currentFrameBack - framesToSubtract);
                                System.out.println("Moved backward by 10 seconds.");
                                break;
                            case "R": // Reset
                                clip[0].setFramePosition(0); // Reset to the beginning of the song
                                System.out.println("Song reset to the beginning.");
                                break;
                            default:
                                System.out.println("Invalid choice.");
                        }
                    }
                }).start();

                // Wait until the song finishes playing or user chooses to skip
                synchronized (clip[0]) {
                    while (clip[0].isRunning()) {
                        try {
                            clip[0].wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                clip[0].close(); // Close the clip after the song finishes

            } catch (Exception e) {
                System.out.println("Error playing song: " + e.getMessage());
            }
        }
    }
}
