// Copyright 2016 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package bot;

import java.util.Scanner;

import move.Move;

/**
 * BotParser class
 *
 * Main class that will keep reading output from the engine. Will either update
 * the bot state or get actions.
 *
 * @author Jim van Eeden <jim@starapple.nl>
 */

public class BotParser {

    final Scanner scan;
    final OmegaGoBot bot;

    private GameState currentState;

    public BotParser( OmegaGoBot bot ) {
        this.scan = new Scanner( System.in );
        this.bot = bot;
        this.currentState = new GameState();
    }

    public void run() {
        while ( this.scan.hasNextLine() ) {
            String line = this.scan.nextLine();

            if ( line.length() == 0 ) {
                continue;
            }

            String[] parts = line.split( " " );
            if ( parts[0].equals( "settings" ) ) {
                this.currentState.parseSettings( parts[1], parts[2] );
            } else if ( parts[0].equals( "update" ) ) { /* new game data */
                if ( parts[1].equals( "game" ) ) {
                    this.currentState.parseGameData( parts[2], parts[3] );
                } else {
                    this.currentState.parsePlayerData( parts[1], parts[2], parts[3] );
                }
            } else if ( parts[0].equals( "action" ) ) {
                if ( parts[1].equals( "move" ) ) { /* move requested */
                    Move move = this.bot.getMove( this.currentState, Integer.parseInt( parts[2] ) );
                    if ( move != null ) {
                        System.out.println( "place_move " + move.getCol() + " " + move.getRow() );
                    } else {
                        System.out.println( "pass" );
                    }
                }
            } else {
                System.out.println( "unknown command" );
            }
        }
    }
}