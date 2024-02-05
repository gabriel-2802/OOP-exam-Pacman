import java.util.*;

interface Character {
    void move();
}

abstract class GameEntity implements Character {
    protected Coordinate position;
    public GameEntity(int x, int y) {
        position = new Coordinate(x, y);
    }

    public Coordinate getPosition() {
        return position;
    }

    public void setPosition(Coordinate position) {
        this.position = position;
    }

    public abstract void move();
}

class Coordinate {
    private int x, y;

    Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Coordinate c) {
        return this.x == c.x && this.y == c.y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

}

class PackMan extends GameEntity {
    List <java.lang.Character> path = new LinkedList<>();
    private final int maxDistance;

    PackMan(int x, int y, int n) {
        super(x, y);
        maxDistance = n;
    }

    @Override
    public void move() {
        if (path.isEmpty()) {
            return;
        }

        char c = path.remove(0);
        switch (c) {
            case 'U':
                position.setY(position.getY() == maxDistance ? position.getY() : position.getY() + 1);
                break;
            case 'D':
                position.setY(position.getY() == 1 ? position.getY() : position.getY() - 1);
                break;
            case 'L':
                position.setX(position.getX() == 1 ? position.getX() : position.getX() - 1);
                break;
            case 'R':
                position.setX(position.getX() == maxDistance ? position.getX() : position.getX() + 1);
                break;
        }
    }

    public void addPath(char c) {
        path.add(c);
    }

    @Override
    public String toString() {
        return position.getX() + " " + position.getY();
    }

}

enum GhostColor {
    BLUE, RED
}

abstract class Ghost extends GameEntity {
    protected int movement;
    protected int maxDistance;

    public Ghost(int x, int y) {
        super(x, y);
    }

    @Override
    public abstract void move();
    public abstract void updateDirection();
    public abstract GhostColor getColor();

}

// left <=> right movement
class RedGhost extends Ghost {

    RedGhost(int x, int y, int n) {
        super(x,y);
        maxDistance = n;
        position = new Coordinate(x, y);
        movement = -1;
    }

    @Override
    public void move() {
        int xNext = position.getX() + movement;
        if (xNext < 1 || xNext > maxDistance) {
            updateDirection();
            position.setX(position.getX() + movement);
        } else {
            position.setX(position.getX() + movement);
        }
    }

    @Override
    public void updateDirection() {
        movement = movement == 1 ? -1 : 1;
    }

    @Override
    public GhostColor getColor() {
        return GhostColor.RED;
    }

    @Override
    public String toString() {
        return "R " + position.getX() + " " + position.getY();
    }


}

// up <=> down movement
class BlueGhost extends Ghost {

    BlueGhost(int x, int y, int n) {
        super(x,y);
        maxDistance = n;
        position = new Coordinate(x, y);
        movement = -1;
    }

    @Override
    public void move() {
        int yNext = position.getY() + movement;
        if (yNext < 1 || yNext > maxDistance) {
            updateDirection();
            position.setY(position.getY() + movement);
        } else {
            position.setY(position.getY() + movement);
        }
    }

    @Override
    public void updateDirection() {
        movement = movement == -1 ? 1 : -1;
    }

    @Override
    public GhostColor getColor() {
        return GhostColor.BLUE;
    }

    @Override
    public String toString() {
        return "B " + position.getX() + " " + position.getY();
    }
}

interface GameReader {
    void readGame(GameBoard game) throws Exception;
}

interface GamePrinter {
    void printGame(GameBoard game);

}

interface GhostFactory {
    Ghost createGhost(int x, int y, int n);
}

class RedGhostFactory implements GhostFactory {
    @Override
    public Ghost createGhost(int x, int y, int n) {
        return new RedGhost(x, y, n);
    }
}

class BlueGhostFactory implements GhostFactory {
    @Override
    public Ghost createGhost(int x, int y, int n) {
        return new BlueGhost(x, y, n);
    }
}

class GReader implements GameReader {
    @Override
    public void readGame(GameBoard game) throws Exception {
        Scanner scan = new Scanner(System.in);
        int n = scan.nextInt();
        game.setBoardSize(n);

        int x = scan.nextInt();
        int y = scan.nextInt();
        game.setPackMan(new PackMan(x, y, n));

        readGhosts(scan, game, new RedGhostFactory());
        readGhosts(scan, game, new BlueGhostFactory());

        int packManPathCount = scan.nextInt();
        game.setMoveCount(packManPathCount);
        for (int i = 0; i < packManPathCount; i++) {
            char c = scan.next().charAt(0);
            checkPath(c);
            game.getPackMan().addPath(c);
        }
    }

    private void readGhosts(Scanner scan, GameBoard game, GhostFactory factory) throws Exception {
            int count = scan.nextInt();
            for (int i = 0; i < count; i++) {
                int x = scan.nextInt();
                int y = scan.nextInt();
                game.getGhosts().add(factory.createGhost(x, y, game.getBoardSize()));
            }
    }

    private void checkPath(char c) throws Exception {
        if (c != 'U' && c != 'D' && c != 'L' && c != 'R')
            throw new Exception("Invalid path");
    }
}

/*
Se cere ca la finalul simularii (dupa M pasi sau cand PacMan moare, daca e cazul) sa afisati, pe cate un rand, coordonatele X,Y ale fiecarui personaj in urmatoarea ordine:
- pozitia lui PacMan
- pentru fiecare din fantome, pe cate un rand, un indicator al culorii si pozitia (B X Y pentru albastre, respectiv R X Y pentru rosii), in ordinea apropierii de marginea stanga a tablei; daca doua fantome sunt pe aceeasi coloana, se vor afisa in ordinea apropierii de marginea de sus a tablei; daca doua fantome sunt pe aceeasi pozitie, se vor afisa intai cele albastre (B) apoi cele rosii (R).
 */
class GPrinter implements GamePrinter {
    @Override
    public void printGame(GameBoard game) {
        System.out.println(game.getPackMan());
        game.getGhosts().stream().sorted((g1, g2) -> {
            if (g1.getPosition().getX() == g2.getPosition().getX()) {
                if (g1.getPosition().getY() == g2.getPosition().getY()) {
                    return g1.getColor().compareTo(g2.getColor());
                }
                return g1.getPosition().getY() - g2.getPosition().getY();
            }
            return g1.getPosition().getX() - g2.getPosition().getX();
        }).forEach(System.out::println);
    }
}

class GameBoard {
    private int boardSize;
    private int moveCount;
    private PackMan packMan;
    private List<Ghost> ghosts = new ArrayList < > ();

    public void play() {
        if (checkCollision()) {
            return;
        }

        for (int i = 0; i < moveCount; i++) {
            packMan.move();
            ghosts.forEach(Ghost::move);
            if (checkCollision()) {
                return;
            }
        }
    }

    private boolean checkCollision() {
        return ghosts.stream().anyMatch(g -> g.getPosition().equals(packMan.getPosition()));
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void setMoveCount(int moveCount) {
        this.moveCount = moveCount;
    }

    public PackMan getPackMan() {
        return packMan;
    }

    public void setPackMan(PackMan packMan) {
        this.packMan = packMan;
    }

    public List<Ghost> getGhosts() {
        return ghosts;
    }

}

public class Main {

    public static void main(String[] args) {

       GameBoard game = new GameBoard();
       GameReader reader = new GReader();
       GamePrinter printer = new GPrinter();

       try {
           reader.readGame(game);
       } catch (Exception e) {
              System.out.println(e.getMessage());
              return;
       }

       game.play();
       printer.printGame(game);
    }
}
